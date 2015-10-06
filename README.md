android-metadata
==================

[![GitHub version](https://badge.fury.io/gh/onehilltech%2Fandroid-metadata.svg)](http://badge.fury.io/gh/onehilltech%2Fandroid-metadata)
[![Build Status](https://travis-ci.org/onehilltech/android-metadata.svg)](https://travis-ci.org/onehilltech/android-metadata)
[![codecov.io](http://codecov.io/github/onehilltech/android-metadata/coverage.svg?branch=master)](http://codecov.io/github/onehilltech/android-metadata?branch=master)

A utility library for Android designed to simpify reading meta-data
values from AndroidManifest.xml.

* **Quickly** access a meta-data values from anywhere with few lines of code.
* Read individual meta-data values into **type-specific** variables.
* Read one or more meta-data values into **annotated** Java classes.

## Installation

#### Gradle

```
buildscript {
  repositories {
    jcenter()
  }
}

dependencies {
  compile com.onehilltech.android:android-metadata:x.y.z
}
```

## Getting Started

Here is the quickest and easiest way to load the metadata from AndroidManifest.xml
and get a value. The value, by default, is a String value type.

```java
ManifestMetadata metadata = ManifestMetadata.get (context);

// <meta-data android:name="appid" android:value="32ba65ae723940" />
String value = metadata.getValue ("appid");
```

If the value is not a String type, then you can provide a hint:

```java
ManifestMetadata metadata = ManifestMetadata.get (context);

// <meta-data android:name="conn.timeout" android:value="60" />
Integer connTimeout = metadata.getValue ("conn.timeout", Integer.class);
```

You can even directly load a resource from the metadata:

```java
ManifestMetadata metadata = ManifestMetadata.get (context);

// <meta-data android:name="appname" android:resource="@string/app_name" />
String appName = metadata.getValue ("appname", true, String.class);
```

In some cases, you may need to provide additional information about
the resource type since different resources types can have the same 
Java type:

```java
ManifestMetadata metadata = ManifestMetadata.get (context);

// <meta-data android:name="bgcolor" android:resource="@color/background" />
Integer bgColor = metadata.getValue ("bgcolor", true, Integer.class, ResourceType.Color);
```

## Using Annotations to Load Metadata

Here is the simplest example of using an annotation to define what 
meta-data value in AndroidManifest.xml it should be initialized with:

```java
public class MyData {
  private String appid_;
  
  @MetadataProperty (name="my.message")
  public String message;
  
  @MetadataMethod (name="appid")
  public void setAppId (String appid) {
    this.appid_ = appid;
  }
}
```

In the example above, the field **message** will be initialized with 
the value of meta-data tag named **my.message**. You initialize all 
values with the **@Metadata** annotation using a single line of code:

```java
MyData myData = new MyData ();
ManifestMetadata.get (context).initFromMetadata (myData);
```

This method will auto-detect the target type, and then assign the value. 
If the field is not assignable using the meta-data's value, then an 
exception will be thrown.

### Reading from a Resource

In some cases, you will want to read the value from a resource (i.e., 
you use android:resource in the meta-data tag). You can use the **@Metadata** 
annotation to read resource values as well:

```java
public class MyData {
  private String appid_;
  
  @MetadataProperty (name="my.message", fromResource=true)
  public String message;

  @MetadataMethod (name="appid", fromResource=true)
  public void setAppId (String appid) {
    this.appid_ = appid;
  }
}
```

### Giving Resource Type Hints

There are some resources that have the same field type, such as integer 
and color. This makes it hard to auto-detect the resources type. We can
therefore provide a hint as follows:

```java
public class MyData {
  @MetadataProperty (name="my.message", fromResource=true, resourceType=ResourceType.Color)
  public int backgroundColor;
}
```

In the example above, the value for **backgroundColor** will be loaded 
from resources and interpreted as a color.
 
