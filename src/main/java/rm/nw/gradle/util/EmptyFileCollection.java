//package rm.nw.gradle.util;
//
//import java.io.File;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.gradle.api.internal.file.AbstractFileCollection;
//
//public class EmptyFileCollection extends AbstractFileCollection {
//  
//  static EmptyFileCollection instance;
//  
//  @Override
//  public Set<File> getFiles() {
//    return new HashSet<File>();
//  }
//
//  @Override
//  public String getDisplayName() {
//    return "Empty File Collection";
//  }
//  
//  public synchronized static EmptyFileCollection getInstance() {
//    if (instance==null) {
//      instance = new EmptyFileCollection();
//    }
//    return instance;
//  }
//
//}
