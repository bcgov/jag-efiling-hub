const { expect } = require('chai')
const { changeowner } = require('.')

describe('change owner', ()=>{

    it('is available', (done)=>{
        changeowner((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns json', (done)=>{
        changeowner((response)=>{
            expect(response.headers['content-type']).to.equal('application/json');
            done();
        })
    })

    it('returns expected data', (done)=>{
        changeowner((response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.equal(JSON.stringify({ Status:'Success' }))
                done();
            });
        })
    })
})
