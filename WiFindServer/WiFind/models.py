from django.db import models
from django.contrib.auth.models import (BaseUserManager, AbstractBaseUser, PermissionsMixin)

class UserManager(BaseUserManager):
    def create_user(self, email,password=None):
        if not email:
            msg = 'Users must have an email address'
            raise ValueError(msg)


        user = self.model(email=UserManager.normalize_email(email))

        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self,email,password):
        user = self.create_user(email,password=password)
        user.is_admin = True
        user.is_staff = True
        user.is_superuser = True
        user.save(using=self._db)
        return user


class WiFindUser(AbstractBaseUser, PermissionsMixin):
    email = models.EmailField(verbose_name='email address',max_length=255,unique=True,db_index=True)

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = []

    is_active = models.BooleanField(default=True)
    is_admin = models.BooleanField(default=False)
    is_staff = models.BooleanField(default=False)

    objects = UserManager()

    def get_full_name(self):
        return self.email

    def get_short_name(self):
        return self.email

    def __unicode__(self):
        return self.email


class Building(models.Model):
    name = models.CharField(max_length=40)

    def __unicode__(self):
        return self.name


class Floor(models.Model):
    level = models.IntegerField()
    building = models.ForeignKey(Building)

    def __unicode__(self):
        return str(self.building) + " - " + str(self.level)


class Area(models.Model):
    name = models.CharField(max_length=40)
    floor = models.ForeignKey(Floor)

    def __unicode__(self):
        return str(self.floor) + " - " + str(self.name)

class Device(models.Model):
    mac = models.CharField(max_length=24)
    os = models.CharField(max_length=20)
    model_name = models.CharField(max_length=40)

    def __unicode__(self):
        return str(self.mac)


class SampleData(models.Model):
    timestamp = models.BigIntegerField()
    area = models.ForeignKey(Area)
    device = models.ForeignKey(Device)
    readings = models.CharField(max_length=3000)