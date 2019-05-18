const { expect } = require('chai')
const { isAuthorized } = require('.')
var fs = require('fs')
var path = require('path')

describe('is authorized', ()=>{

    it('is available', (done)=>{
        isAuthorized((response)=>{
            expect(response.statusCode).to.equal(200);
            done();
        })
    })

    it('returns xml', (done)=>{
        isAuthorized((response)=>{
            expect(response.headers['content-type']).to.equal('text/xml');
            done();
        })
    })

    it('returns expected data', (done)=>{
        var expected = fs.readFileSync(path.join(__dirname, 'data', 'authorized.xml')).toString();
        isAuthorized((response)=>{
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
