# Create your views here.

from django.http import HttpResponse
import json

def process(js):
    i=0
    print "Building: ", js['Location']['Building']
    print "Floor: ",js['Location']['Floor']
    print "ID: ",js['Location']['ID']

    for id in js['Readings']:
        print "SSID: ",js['Readings'][id]['SSID']
        print "BSSID: ",js['Readings'][id]['BSSID']
        print "Strength: ",js['Readings'][id]['Strength']
        i+=1

    print "Total Readings: ",i


def respond(request):
    if request.method == 'GET':
        html = "<html><body>GET Recevied!</body></html>"

    elif request.method == 'POST':
        html = "<html><body>You sent POST! </body></html>"
        js = json.loads(request.body)
        #print js
        process(js)

    else:
        html = "<html><body>Unknown Method!</body></html>"
    return HttpResponse(html)