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

package com.onehilltech.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @interface Metadata
 * 
 * Metadata annotation for setter methods of a class. The setter method
 * can only have 1 parameter---the value read from metadata.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MetadataMethod
{
  /// Name of the metadata.
  String name ();
  
  /// The metadata value comes from a resource.
  boolean fromResource () default false;
  
  /// Get the hint for the resource type.
  ResourceType resourceType () default ResourceType.Auto;
}
