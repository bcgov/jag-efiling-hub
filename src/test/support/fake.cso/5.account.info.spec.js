const { expect } = require('chai')
const { accountInfo } = require('.')
var fs = require('fs')
var path = require('path')

describe('account info', ()=>{

    it('is available', (done)=>{
        accountInfo((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        accountInfo((response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'account.xml')).toString();
        accountInfo((response)=>{
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
