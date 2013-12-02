# Create your views here.

from django.http import HttpResponse
import json
from analyse import get_cluster, load_access_points_academic, euclidean_location

def process(js):
    i=0
    print "Device MAC: ",js['Device']['MAC']
    print "Device Name: ",js['Device']['Name']
    print "Device OS: ",js['Device']['OS Build']
    print "Building: ", js['Location']['Building']
    print "Floor: ",js['Location']['Floor']
    print "ID: ",js['Location']['ID']
    print "Occupancy", js['Location']['Occupancy']
    print "Timestamp", js['Time']

    for id in js['Readings']:
        print "SSID: ",js['Readings'][id]['SSID']
        print "BSSID: ",js['Readings'][id]['BSSID']
        print "Strength: ",js['Readings'][id]['Strength']
        print
        i+=1

    print "Total Readings: ",i


def respond(request):
    if request.method == 'GET':
        html = "<html><body>GET Recevied!</body></html>"

    elif request.method == 'POST':
        html = "Your results have been recorded :)"
        js = json.loads(request.body)
        print js

        # path = '/home/prateek/Desktop/Readings.txt'
        #
        # f = open(path,'a+')
        # json.dump(js,f)
        # f.write("\n")
        # f.close()
        #
        # process(js)

    else:
        html = "<html><body>Unknown Method!</body></html>"
    return HttpResponse(html)

def query(request):
    if request.method == 'GET':
        html = "<html><body>GET Recevied!</body></html>"

    elif request.method == 'POST':
        js = json.loads(request.body)
        #print js

        res = estimate_location(js)
    else:
        html = "<html><body>Unknown Method!</body></html>"
    return HttpResponse(res)

def estimate_location(js):
    a = {}
    for access_point in load_access_points_academic():
        a[access_point] = [0,0]

    for reading in js['Readings']:
        cluster = get_cluster(js['Readings'][reading]["BSSID"])

        if cluster in a:
            a[cluster][0] += int(js['Readings'][reading]["Strength"])
            a[cluster][1] +=1
    for access_point in a:
        if (a[access_point][1]==0):
            a[access_point] = -99
        else:
            a[access_point] = a[access_point][0] / a[access_point][1]
    # print a

    location = euclidean_location(a)
    return location