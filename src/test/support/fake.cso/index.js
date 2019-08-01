const { createServer, get, request } = require('http')
const port = 5005
const read = (file)=>{
    var fs = require('fs')
    var path = require('path')
    var data = fs.readFileSync(path.join(__dirname, 'data', file)).toString()

    return data
}
const server = {
    start: function(done) {
        this.internal = createServer((request, response)=>{
            console.log(request.method, request.url, request.headers)
            let answer = JSON.stringify({ alive:true })
            if (request.url == '/cso-extension') {
                var body = ''
                request.on('data', (chunk)=>{
                    body += chunk
                })
                request.on('end',()=>{
                    console.log('body', body);
                    response.setHeader('content-type', 'text/xml')
                    let action = request.headers['soapaction']
                    if (action == 'account-info') {
                        answer = body.indexOf('<accountId>111</accountId>')!=-1 ? read('account.jd.xml') : read('account.any.xml')
                    }
                    else if (action == 'is-authorized') {
                        answer = body.indexOf('<userguid>JD</userguid>')!=-1 ? read('authorized.jd.xml') : read('authorized.any.xml')
                    }
                    else if (action == 'save-filing') {
                        answer = read('savefiling.ok.xml')
                    }
                    else if (action == 'payment-process') {
                        answer = read('payment.ok.xml')
                    }
                    console.log('answering with', answer);
                    response.write(answer)
                    response.end();
                })

            }
            else {
                if (request.url == '/search') {
                    response.setHeader('content-type', 'text/xml')
                    if (request.headers['soapaction'] == 'second-call') {
                        answer = read('basics.xml')
                    }
                    else if (request.headers['soapaction'] == 'third-call') {
                        answer = read('parties.xml')
                    }
                    else {
                        answer = '<CaseId>12345</CaseId><CaseType>Civil</CaseType>'
                    }
                }
                else {
                    response.setHeader('content-type', 'application/json')
                }
                console.log('answering with', answer);
                response.write(answer)
                response.end();
            }
        })
        this.internal.listen(port, done);
    }
}
const getRequest = (path, headers)=>{
    return (verify)=>{
        get({ path:path, port:port, headers:headers }, (response)=>{ verify(response) })
    }
}
const postRequest = (path, headers)=>{
    return (body, verify)=>{
        var action = { path:path, port:port, headers:headers, method:'POST' }
        var post = request(action, (response)=>{ verify(response) })
        post.write(body)
        post.end()
    }
}

module.exports = {
    ping:getRequest('/ping'),
    search:getRequest('/search'),
    basics:getRequest('/search', { 'SOAPAction':'second-call' }),
    parties:getRequest('/search', { 'SOAPAction':'third-call' }),
    accountInfo:postRequest('/cso-extension', { 'SOAPAction':'account-info' }),
    isAuthorized:postRequest('/cso-extension', { 'SOAPAction':'is-authorized' }),
    payment:postRequest('/cso-extension', { 'SOAPAction':'payment-process' }),
    savefiling:postRequest('/cso-extension', { 'SOAPAction':'save-filing' })
}

server.start(()=>{
    console.log('listening on port', port)
})
