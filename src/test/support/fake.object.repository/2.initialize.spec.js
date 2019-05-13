const { expect } = require('chai')
const { initialize } = require('.')

describe('initialize', ()=>{

    it('is available', (done)=>{
        initialize((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns json', (done)=>{
        initialize((response)=>{
            expect(response.headers['content-type']).to.equal('application/json');
            done();
        })
    })

    it('returns expected data', (done)=>{
        initialize((response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.equal(JSON.stringify({ AppTicket:'ticket-from-fake-object-repository' }))
                done();
            });
        })
    })
})
