const { createServer, get } = require('http')
const port = 5010

const server = {
    start: function(done) {
        this.internal = createServer((request, response)=>{
            console.log(request.method, request.url)
            let params = require('url').parse(request.url)
            if (params.pathname == '/initialize') {
                response.setHeader('content-type', 'application/json')
                response.write(JSON.stringify({ AppTicket:'ticket-from-fake-object-repository' }))
            }
            else if (params.pathname == '/create') {
                response.setHeader('content-type', 'application/json')
                response.write(JSON.stringify({ Object_GUID:'guid-from-fake-object-repository' }))
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
    initialize:request('/initialize'),
    create:request('/create?AppTicket=any')
}

server.start(()=>{
    console.log('listening on port', port)
})
