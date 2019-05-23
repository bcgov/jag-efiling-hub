const { expect } = require('chai')
const { isAuthorized } = require('.')
var fs = require('fs')
var path = require('path')

describe('is authorized for someone else', ()=>{

    it('is available', (done)=>{
        isAuthorized('anything-before<userguid>MAX</userguid>anythin-after', (response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        isAuthorized('anything-before<userguid>MAX</userguid>anythin-after', (response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'authorized.any.xml')).toString();
        isAuthorized('anything-before<userguid>MAX</userguid>anythin-after', (response)=>{
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
