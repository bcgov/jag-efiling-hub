const { expect } = require('chai')
const { create } = require('.')

describe('create', ()=>{

    it('is available', (done)=>{
        create((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns json', (done)=>{
        create((response)=>{
            expect(response.headers['content-type']).to.equal('application/json');
            done();
        })
    })

    it('returns expected data', (done)=>{
        create((response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.equal(JSON.stringify({ Object_GUID:'guid-from-fake-object-repository' }))
                done();
            });
        })
    })
})
