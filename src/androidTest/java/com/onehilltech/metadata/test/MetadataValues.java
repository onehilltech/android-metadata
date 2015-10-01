/**
 * Copyright (C) 2013, James H. Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onehilltech.metadata.test;

import android.content.res.XmlResourceParser;

import com.onehilltech.metadata.MetadataMethod;
import com.onehilltech.metadata.MetadataProperty;
import com.onehilltech.metadata.ResourceType;

public class MetadataValues
{
  //// Attribute Testers
  
  // android:value
  @MetadataProperty (name="metadata.string")
  public String theString;
  
  @MetadataProperty(name="metadata.integer")
  public int theInteger;
  
  @MetadataProperty(name="metadata.classname")
  public Class <TestClass> theClass;

  // android:resource
  @MetadataProperty(name="metadata.resource.string", fromResource=true)
  public String theStringResource;

  @MetadataProperty(name="metadata.resource.integer", fromResource=true)
  public int theIntegerResource;
  
  @MetadataProperty(name="metadata.resource.animation", fromResource=true)
  public XmlResourceParser theAnimation;

  @MetadataProperty(name="metadata.resource.boolean.true", fromResource=true)
  public boolean theTrueValue;

  @MetadataProperty(name="metadata.resource.boolean.false", fromResource=true)
  public Boolean theFalseValue;

  @MetadataProperty(name="metadata.resource.dimension", fromResource=true)
  public float theDimension;
  
  @MetadataProperty(name="metadata.resource.color", fromResource=true, resourceType= ResourceType.Color)
  public int colorBlack;
  
  //// Method Testers
  
  private String metadataString_;
  
  @MetadataMethod (name="metadata.string")
  public void setMetadataString (String str)
  {
    this.metadataString_ = str;
  }
  
  public String getMetadataString ()
  {
    return this.metadataString_;
  }
}