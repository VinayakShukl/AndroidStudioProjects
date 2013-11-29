from django.conf.urls import patterns, include, url
from WiFind import views
# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'WiFindServer.views.home', name='home'),
    # url(r'^WiFindServer/', include('WiFindServer.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),
    url(r'^alive/$',views.alive),
    url(r'^check_post/$',views.check_post),
    url(r'^new_sample/$',views.new_sample),
    url(r'^existing_mac/$',views.existing_mac),
    url(r'^new_user/$',views.new_user),
    url(r'^confirm_code/$',views.confirm_code),
    url(r'^new_device/$',views.new_device),
    url(r'^login/$',views.login_user),
    url(r'^test_login/$',views.test_login),
)
