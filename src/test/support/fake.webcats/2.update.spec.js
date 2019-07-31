const { expect } = require('chai')
const { update } = require('.')
const fs = require('fs')
const path = require('path')

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
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'update.ok.xml')).toString();
        update((response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.equal(expected)
                done();
            });
        })
    })
})
