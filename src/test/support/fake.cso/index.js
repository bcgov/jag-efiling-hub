const { createServer, get } = require('http')
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
                response.setHeader('content-type', 'text/xml')
                let action = request.headers['soapaction']
                answer = action == 'account-info' ? read('account.xml') : read('authorized.xml')
            }
            else if (request.url == '/search') {
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
        })
        this.internal.listen(port, done);
    }
}
const request = (path, headers)=>{
    return (verify)=>{
        get({ path:path, port:port, headers:headers }, (response)=>{ verify(response) })
    }
}

module.exports = {
    ping:request('/ping'),
    search:request('/search'),
    basics:request('/search', { 'SOAPAction':'second-call' }),
    parties:request('/search', { 'SOAPAction':'third-call' }),
    accountInfo:request('/cso-extension', { 'SOAPAction':'account-info' }),
    isAuthorized:request('/cso-extension', { 'SOAPAction':'is-authorized' })
}

server.start(()=>{
    console.log('listening on port', port)
})
