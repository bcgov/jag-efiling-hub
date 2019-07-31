const { expect } = require('chai')
const { update } = require('.')

describe('update', ()=>{

    it('is available', (done)=>{
        update((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        update((response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        update((response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.equal('<return><update>ok</update></return>')
                done();
            });
        })
    })
})
