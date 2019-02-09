const { expect } = require('chai')
const { search } = require('.')

describe('search', ()=>{

    it('is available', (done)=>{
        search((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        search((response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        search((response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.deep.equal('<CaseId>12345</CaseId>')
                done();
            });
        })
    })
})
