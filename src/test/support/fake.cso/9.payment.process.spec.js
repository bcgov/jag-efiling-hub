const { expect } = require('chai')
const { payment } = require('.')
var fs = require('fs')
var path = require('path')

describe('payment process', ()=>{

    it('is available', (done)=>{
        payment('anything-before<userguid>JD</userguid>anything-after', (response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        payment('anything-before<userguid>JD</userguid>anything-after', (response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'payment.ok.xml')).toString();
        payment('anything-before<userguid>JD</userguid>anything-after', (response)=>{
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
