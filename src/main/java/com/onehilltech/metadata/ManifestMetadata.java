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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Utility class for loading meta-data from AndroidManifest.xml. This
 * class defines methods that convert meta-data values into type-specific
 * values. It also provides methods for initializing annotated classes
 * with values defined in the meta-data.
 * 
 * The class is a Singleton, and must be access using the get () method.
 * We use the Singleton pattern since there is only one manifest for a
 * mobile application, and we can better control access to it (and its
 * memory usage) via the singleton object.
 */
public class ManifestMetadata 
{ 
  /// The metadata bundle
  private Bundle metadata_;
  
  /// The application context for the program.
  private Context context_;
  
  /// Singleton reference.
  private static WeakReference <ManifestMetadata> instance_;
  
  private static final String TAG = ManifestMetadata.class.getName ();
  
  /**
   * Get the singleton instance of the manifest. W
   * @param context Execution context
   * @return
   * @throws NameNotFoundException
   */
  public static ManifestMetadata get (Context context)
      throws NameNotFoundException
  {
    if (instance_ != null && instance_.get () != null)
      return instance_.get ();
    
    ManifestMetadata mm = new ManifestMetadata (context);
    instance_ = new WeakReference <ManifestMetadata> (mm);
    
    return instance_.get ();
  }
  
  /**
   * Default constructor.
   * 
   * @param context
   * @throws NameNotFoundException
   */
  private ManifestMetadata (Context context) 
      throws NameNotFoundException
  {
    PackageManager pm = context.getPackageManager ();    
    ApplicationInfo ai = pm.getApplicationInfo (context.getPackageName (), PackageManager.GET_META_DATA);
   
    this.metadata_ = ai.metaData;
    this.context_ = context.getApplicationContext ();
  }
  
  public Bundle getMetadata ()
  {
    return this.metadata_;
  }
  
  /**
   * Get the value of a meta-data element in AndroidManifest.xml. The
   * value is returned as a String value type.
   * 
   * @param       name        Name of the meta-data element
   * @return      Value of the meta-data element
   */
  public String getValue (String name)
  {
    return this.metadata_.getString (name);
  }
  
  /**
   * Get the value of a metadata element in AndroidManifest.xml. If the 
   * element exist, then a generic Object is return. If the value does not
   * exist, then an exception is thrown.
   */
  public <T> T getValue (String name, Class <T> typeHint)
    throws NameNotFoundException, IllegalArgumentException, 
      ClassNotFoundException, IllegalAccessException, InvocationTargetException
  {
    return this.getValue (name, false, ResourceType.Auto, typeHint);
  }

  public <T> T getValue (String name, boolean fromResource, Class <T> typeHint)
    throws NameNotFoundException, IllegalArgumentException, 
      ClassNotFoundException, IllegalAccessException, InvocationTargetException
  {
    return getValue (name, fromResource, ResourceType.Auto, typeHint);
  }

  @SuppressWarnings ("unchecked")
  public <T> T getValue (String name, 
                         boolean fromResource, 
                         ResourceType resourceType,
                         Class <T> typeHint)
    throws NameNotFoundException, IllegalArgumentException, 
      ClassNotFoundException, IllegalAccessException, InvocationTargetException
  {
    if (!this.metadata_.containsKey (name))
      throw new NameNotFoundException (name + " not defined in AndroidManifest.xml");
    
    return (T)this.getValueFromMetadata (name,
                                         fromResource,
                                         resourceType,
                                         typeHint);    
  }
  
  /**
   * Load metadata from the manifest and initialize annotated values/methods 
   * in the target object.
   * 
   * @param         target          Instance of object with annotated class
   * @throws NameNotFoundException
   * @throws IllegalAccessException 
   * @throws IllegalArgumentException 
   * @throws ClassNotFoundException 
   * @throws InvocationTargetException 
   */
	public <T> void initFromMetadata (T target) 
	    throws NameNotFoundException, IllegalArgumentException, 
	    IllegalAccessException, ClassNotFoundException, InvocationTargetException
	{
	  // Get the class, and locate fields with @Metadata.
	  Field [] fields = target.getClass ().getFields ();
	  
	  if (fields.length != 0)
	  {
	    // Iterate over each field in the class definition. If we find a
	    // field with the @Metadata annotation, then we need to load its 
	    // value from AndroidManifest.xml.
  	  for (Field field : fields)
  	  {
        if (!field.isAnnotationPresent (MetadataProperty.class))
          continue;
        
	      // Get the annotation value, and the target meta-data name.
	      MetadataProperty annotation = field.getAnnotation (MetadataProperty.class);
	      String targetName = annotation.name ();
	      
	      // If the meta-data name is an empty string, use the field name.
	      if (targetName.equals (""))
	        targetName = field.getName ();
	      
	      // Make sure that metadata does contain the target name before,
	      // or there is no need to continue at this point.
	      if (!this.metadata_.containsKey (targetName))
	        continue;
	      
        // Load the value from the bundle.
        Object theValue =
            this.getValueFromMetadata (targetName, 
                                       annotation.fromResource (),
                                       annotation.resourceType (),
                                       field.getType ());
        
        // Finally, we can set the value!
        field.set (target, theValue);            
  	  }
	  }
	  
	  // Locate all methods with @MetadataMethod
	  Method [] methods = target.getClass ().getMethods ();
	  
	  if (methods.length != 0)
	  {
      // Iterate over each field in the class definition. If we find a
      // field with the @Metadata annotation, then we need to load its 
      // value from AndroidManifest.xml.
	    for (Method method : methods)
	    {
	      if (!method.isAnnotationPresent (MetadataMethod.class))
	        continue;
	      
	      // Get the annotation value, and the target meta-data name. For methods,
	      // the target name is required. 
	      MetadataMethod annotation = method.getAnnotation (MetadataMethod.class);
        String targetName = annotation.name ();
        
        // Make sure that meta-data does contain the target name before,
        // or there is no need to continue at this point.
        if (!this.metadata_.containsKey (targetName))
          continue;
        
        // Load the value from the bundle.
        Object theValue =
            this.getValueFromMetadata (targetName,
                                       annotation.fromResource (),
                                       annotation.resourceType (),
                                       method.getParameterTypes ()[0]);
        
        // Finally, we can set the value!
        method.invoke (target, theValue);    
	    }
	  }
	}
	
	/**
	 * Load a value from meta-data. The returned value is an Object of the
	 * correct type. It is the responsibility of the caller to convert the
	 * returned object to the correct type.
	 * 
	 * For classes, the return object is an instance of the class. The class
	 * therefore must have a default constructor defined. If one is not defined,
	 * then an exception will be thrown.
	 * 
	 * @param      targetName       Name of value in meta-data bundle
	 * @param      typeInfo         Type information about the target property
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private Object getValueFromMetadata (String targetName,
	                                     boolean fromResource,
	                                     ResourceType rcType,
	                                     Class <?> typeInfo) 
	  throws ClassNotFoundException, IllegalArgumentException, 
	         IllegalAccessException, InvocationTargetException
	{
    // Load the value from the bundle.
    Object theValue = this.metadata_.get (targetName);

    // The current value that we are reading from the bundle is actually
    // a resource id. We therefore need to convert the value to an Integer
    // so we can load it from the the resources.
    if (fromResource)
    {
      Integer rcid = (Integer)theValue;
      
      // Either, we are going to auto-detect the resource type based on the
      // type of the field, or we are given a hint in the annotation.
      if (rcType.equals (ResourceType.Auto))
        theValue = this.getValueFromResource (rcid, typeInfo);
      else
        theValue = this.getValueFromResource (rcid, rcType);
    }
    
    if (typeInfo.equals (Class.class))
    {
      // The value is a Class object. Let's load the class object.
      ClassLoader classLoader = this.context_.getClassLoader ();
      theValue = classLoader.loadClass ((String)theValue);
    }
    
    return theValue;
	}
	
	/**
	 * Get the value from a resource. The value type is determined by
	 * the field object type.
	 * 
	 * @param rcid
	 * @return
	 */
	private Object getValueFromResource (int rcid, Class <?> typeInfo)
	{
	  Resources r = this.context_.getResources ();
	  
	  if (typeInfo.isAssignableFrom (String.class))
	    return r.getString (rcid);
	  else if (typeInfo.isAssignableFrom (int.class) || typeInfo.isAssignableFrom (Integer.class))
	    return rcid;
    else if (typeInfo.isAssignableFrom (boolean.class) || typeInfo.isAssignableFrom (Boolean.class))
      return r.getBoolean (rcid);
    else if (typeInfo.isAssignableFrom (float.class) || typeInfo.isAssignableFrom (Float.class))
      return r.getDimension (rcid);
	  else if (typeInfo.isAssignableFrom (int[].class))
	    return r.getIntArray (rcid);
    else if (typeInfo.isAssignableFrom (XmlResourceParser.class))
      return r.getAnimation (rcid);
	  else
	    return null;
	}
	
	/**
	 * Get the value from a resource. The resource type is determined by
	 * the rcType parameter.
	 * 
	 * @param rcid
	 * @param rcType
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
  private Object getValueFromResource (int rcid, ResourceType rcType) 
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
  {
    Method method = resourceTable_.get (rcType);
    return method.invoke (this.context_.getResources (), rcid);
  }
  
  private static final HashMap <ResourceType, Method> resourceTable_ = new HashMap <ResourceType, Method> ();
  
  static
  {
    installResourceMethodAccessor (ResourceType.Animation, "getAnimation");
    installResourceMethodAccessor (ResourceType.Boolean, "getBoolean");
    installResourceMethodAccessor (ResourceType.Color, "getColor");
    installResourceMethodAccessor (ResourceType.Dimension, "getDimension");
    installResourceMethodAccessor (ResourceType.DimensionPixelOffset, "getDimensionPixelOffset");
    installResourceMethodAccessor (ResourceType.DimensionPixelSize, "getDimensionPixelSize");
    installResourceMethodAccessor (ResourceType.Drawable, "getDrawable");
    installResourceMethodAccessor (ResourceType.Id, "getInteger");
    installResourceMethodAccessor (ResourceType.Integer, "getInteger");
    installResourceMethodAccessor (ResourceType.IntArray, "getIntArray");
    installResourceMethodAccessor (ResourceType.String, "getString");
  }
  
  /**
   * Install the resource access method for the specified resource type.
   * 
   * @param rcType
   * @param methodName
   */
  private static void installResourceMethodAccessor (ResourceType rcType, String methodName)
  {
    Class <Resources> clazz = Resources.class;
    
    try
    {
      resourceTable_.put (rcType, clazz.getMethod (methodName, int.class));
    }
    catch (SecurityException e)
    {
      Log.w (TAG, e.getMessage (), e);
    }
    catch (NoSuchMethodException e)
    {
      Log.w (TAG, e.getMessage (), e);
    } 
  }
}