const { createServer, get } = require('http')
const fs = require('fs')
const path = require('path')
const port = 5015

const server = {
    start: function(done) {
        this.internal = createServer((request, response)=>{
            console.log(request.method, request.url)
            let params = require('url').parse(request.url)
            if (params.pathname == '/update') {
                response.setHeader('content-type', 'text/xml')
                var ok = fs.readFileSync(path.join(__dirname, 'data', 'update.ok.xml')).toString()
                response.write(ok)
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
const request = (path)=>{
    return (verify)=>{
        get({ path:path, port:port }, (response)=>{ verify(response) })
    }
}

module.exports = {
    ping:request('/ping'),
    update:request('/update')
}

server.start(()=>{
    console.log('listening on port', port)
})
