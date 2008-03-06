/* Copyright (c) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdata.util;

import com.google.gdata.client.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Version class is a helper class that describes version information 
 * about a particular type of service.
 */
public class Version {
  
  /**
   * The ANY value indicates a version component that will match any revision.
   */
  public static final int ANY = -1;
  
  private Class<? extends Service> serviceClass;
  private int major;
  private int minor;
  private List<Version> impliedVersions = new ArrayList<Version>();
  
  /**
   * Regex that description the format of a version description.   The
   * first (optional) group is the service name, followed by a group
   * containing the major version, and then another optional group that
   * contains the minor version number preceded by a dot ('.').
   */
  private static final Pattern VERSION_PROPERTY_PATTERN = 
    Pattern.compile("([^\\d]+-)?(\\d+)(\\.\\d+)?");

  
  /**
   * Creates a new Version instance for the specified service and defines
   * the major and minor versions for the service.
   * @param serviceClass the service type.
   * @param major the major revision number of the service.
   * @param minor the minor revision number of the service.
   * @throws NullPointerException if the service type is {@code null}.
   * @throws IllegalArgumentException if revision values are invalid.
   */
  public Version(Class<? extends Service> serviceClass, int major, int minor,
      Version ... impliedVersions)
      throws NullPointerException, IllegalArgumentException {
    
    if (serviceClass == null) {
      throw new NullPointerException("Null service class");
    }
    if (major < 0 && major != ANY) {
      throw new IllegalArgumentException("Invalid major version:" + major);
    }
    if (minor < 0 && minor != ANY) {
      throw new IllegalArgumentException("Invalid minor version:" + minor);
    }
    this.serviceClass = serviceClass;
    this.major = major;
    this.minor = minor;
    
    // Compute the full list of implied versions
    computeImpliedVersions(impliedVersions);
  }
  
  /**
   * Creates a new Version instance using a version description with the
   * format <code>[{service}]{major}.{minor}</code>.
   * 
   * @param serviceClass the service type.
   * @param versionDescription the service description.
   * @throws IllegalArgumentException if the versionDescription has an invalid
   *         syntax or includes a service name that does not match the service
   *         type.
   */
  public Version(Class<? extends Service> serviceClass, 
      String versionDescription, Version ... impliedVersions)
      throws IllegalArgumentException {
    this.serviceClass = serviceClass;
    Matcher matcher = VERSION_PROPERTY_PATTERN.matcher(versionDescription);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Version description does not match expected format" +
          "[{service}]{major}[.{minor}]:" + 
          versionDescription);
    }
    String minorValue = matcher.group(3);
    major = Integer.parseInt(matcher.group(2));
    minor = (minorValue != null) ? 
        Integer.parseInt(minorValue.substring(1)) : ANY;
        
   // Compute the full list of implied versions
   computeImpliedVersions(impliedVersions);
  }
  
  /**
   * Returns the service type of the version.
   * @return service type.
   */
  public final Class<? extends Service> getServiceClass() { 
    return serviceClass; 
  }
  
  /**
   * Returns the major revision of the version.
   * @return major revision.
   */
  public final int getMajor() { return major; }

  /**
   * Returns the minor revision of the version.
   * @return minor revision.
   */
  public final int getMinor() { return minor; }
  
  /**
   * Returns the String representation of the version.
   */
  public final String getVersionString() {
    StringBuilder sb = new StringBuilder();
    if (major != ANY) {
      sb.append(major);
    }
    if (minor != ANY) {
      sb.append('.');
      sb.append(minor);
    }
    return sb.toString();
  }
  

  /**
   * Returns {@code true} if the target version is for the same service.
   * @param v target version to check.
   * @return {@code true} if service matches.
   */
  public final boolean isSameService(Version v) {
    return v != null && serviceClass.equals(v.serviceClass);
  }
  
  /**
   * Returns {@code true} if the specified is compatible with this version or
   * one of its implied versions.   Two versions are compatible if they are
   * for the same service and have a matching major version number (or one
   * of them has a major version of {@link #ANY}.
   */
  public final boolean isCompatible(Version v) {
    if (isSameService(v) && 
       (major == v.major || major == ANY || v.major == ANY)) {
      return true;
    } else {
      for (Version impliedVersion : impliedVersions) {
        if (impliedVersion == this) {
          continue;
        }
        if (impliedVersion.isCompatible(v)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Returns the list of related services versions that are implied by this
   * version.
   */
  public List<Version> getImpliedVersions() {
    return impliedVersions;
  }

  /**
   * Compute the fully resolved list of implied versions, including the
   * local instance and all directly and indirectly implied versions.
   * @param versionList the list of directly implied versions.
   */
  private void computeImpliedVersions(Version ... versionList) {
    impliedVersions.add(this);
    for (Version v : versionList) {
      addImpliedVersion(v);
    } 
  }
  
  /**
   * Adds an implied version (plus any of its nested dependencies) to the
   * implied versions list for this version.
   * @param v the implied version.
   */
  private void addImpliedVersion(Version v) {
    if (!impliedVersions.contains(v)) {
      impliedVersions.add(v);
      for (Version impliedVersion : v.getImpliedVersions()) {
        addImpliedVersion(impliedVersion);
      }
    }
  }
  
  @Override 
  public boolean equals(Object o) {
    if (!(o instanceof Version)) {
      return false;
    }
    Version v = (Version) o;
    return isSameService(v) && major == v.major && minor == v.minor;
  }
  
  @Override
  public int hashCode() {
    int result = serviceClass.hashCode();
    result = 37 * result + major;
    result = 37 * result + minor;
    return result;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(serviceClass.getName());
    sb.append(':');
    sb.append(getVersionString());
    return sb.toString();
  }
}