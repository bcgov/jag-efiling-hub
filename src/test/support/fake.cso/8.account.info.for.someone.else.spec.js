const { expect } = require('chai')
const { accountInfo } = require('.')
var fs = require('fs')
var path = require('path')

describe('account info for someone else', ()=>{

    it('is available', (done)=>{
        accountInfo('anything-before<accountId>222</accountId>anythin-after', (response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        accountInfo('anything-before<accountId>222</accountId>anythin-after', (response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'account.any.xml')).toString();
        accountInfo('anything-before<accountId>222</accountId>anythin-after', (response)=>{
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
