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
            console.log(request.method, request.url)
            if (request.url == '/search') {
                response.setHeader('content-type', 'text/xml')
                if (request.headers['soapaction'] == 'second-call') {
                    response.write(read('basics.xml'))
                }
                else if (request.headers['soapaction'] == 'third-call') {
                    response.write(read('parties.xml'))
                }
                else {
                    response.write('<CaseId>12345</CaseId><CaseType>Civil</CaseType>')
                }
            }
            else {
                response.setHeader('content-type', 'application/json')
                response.write(JSON.stringify({ alive:true }))
            }
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
    parties:request('/search', { 'SOAPAction':'third-call' })
}

server.start(()=>{
    console.log('listening on port', port)
})
