const { expect } = require('chai')
const { savefiling } = require('.')
var fs = require('fs')
var path = require('path')

describe('save filing', ()=>{

    it('is available', (done)=>{
        savefiling('anything-before<invoiceNo>123</invoiceNo>anything-after', (response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        savefiling('anything-before<invoiceNo>123</invoiceNo>anything-after', (response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'savefiling.ok.xml')).toString();
        savefiling('anything-before<invoiceNo>123</invoiceNo>anything-after', (response)=>{
            var body = '';
            response.on('data', (chunk) => {
                body += chunk;
            });
            response.on('end', () => {
                expect(body).to.deep.equal(expected)
                done();
            });
        })
    })
})
