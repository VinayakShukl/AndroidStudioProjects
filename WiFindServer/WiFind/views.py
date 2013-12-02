from django.http import HttpResponse,HttpResponseNotAllowed,HttpResponseForbidden
from models import *
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth import login,authenticate

import json

def alive(request):
    return HttpResponse()


def check_post(request):
    if request.method== "POST":
        json_data = json.loads(request.body)

        building = Building.objects.all().get(name=json_data['Location']['Building'])
        print json_data
        #building = building[0]

        floor = Floor.objects.all().filter(level = int(json_data['Location']['Floor'])).get(building=building.id)
        #floor = floor[0]

        area = Area.objects.all().filter(name = json_data['Location']['ID']).get(floor=floor.id)
        #print area
        return HttpResponse("Ok")

    else:
        return HttpResponseNotAllowed("Invalid method.")


def new_sample(request):
    pass


def existing_mac(request):
    return HttpResponse("True")


def new_user(request):
    return HttpResponse("True")


def confirm_code(request):
    return HttpResponse("True")


def new_device(request):
    return HttpResponse("True")

@csrf_exempt
def login_user(request):
    username = ''
    password = ''

    if request.POST:
        username = request.POST.get('username')
        password = request.POST.get('password')

        user = authenticate(username=username, password=password)
        if user is not None:
            if user.is_active:
                login(request,user)
                print "Logged in."
                return HttpResponse("Logged in.")
        print "Invalid Credentials"
        return HttpResponseForbidden("Invalid Credentials!")
    print "Unknown HTTP method."
    return HttpResponse("Unknown HTTP method.")

def test_login(request):
    if not request.user.is_anonymous():
        print request.user.email
        return HttpResponse("You are: " + request.user.email)
    else:
        return HttpResponseForbidden("Not logged in.")
