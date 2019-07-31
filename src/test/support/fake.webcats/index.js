const { createServer, get } = require('http')
const port = 5015

const server = {
    start: function(done) {
        this.internal = createServer((request, response)=>{
            console.log(request.method, request.url)
            let params = require('url').parse(request.url)
            if (params.pathname == '/update') {
                response.setHeader('content-type', 'text/xml')
                response.write('<return><update>ok</update></return>')
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
