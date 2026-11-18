package dev.legacyconsole.port;
import java.io.*;import java.util.*;
public final class Configuration {
 private final File file;private final Properties values=new Properties();private boolean changed;
 public Configuration(File file){this.file=file;if(file.isFile())try(FileInputStream input=new FileInputStream(file)){values.load(input);}catch(IOException e){throw new IllegalStateException("Cannot read console settings",e);}}
 public final class Property {private final String key;Property(String key,Object fallback){this.key=key;if(!values.containsKey(key)){values.setProperty(key,fallback.toString());changed=true;}}public boolean getBoolean(){return Boolean.parseBoolean(values.getProperty(key));}public void set(boolean value){set((Object)value);}public void set(int value){set((Object)value);}public void set(double value){set((Object)value);}private void set(Object value){values.setProperty(key,value.toString());changed=true;}}
 public Property get(String category,String name,boolean fallback){return new Property(category+"."+name,fallback);}public Property get(String category,String name,int fallback){return new Property(category+"."+name,fallback);}public Property get(String category,String name,double fallback){return new Property(category+"."+name,fallback);}
 public boolean getBoolean(String name,String category,boolean value,String help){return get(category,name,value).getBoolean();}
 public int getInt(String name,String category,int value,int min,int max,String help){Property p=get(category,name,value);try{return Math.max(min,Math.min(max,Integer.parseInt(values.getProperty(p.key))));}catch(NumberFormatException e){return value;}}
 public float getFloat(String name,String category,float value,float min,float max,String help){Property p=get(category,name,(double)value);try{return Math.max(min,Math.min(max,Float.parseFloat(values.getProperty(p.key))));}catch(NumberFormatException e){return value;}}
 public Category getCategory(String name){return new Category(name);}public final class Category{private final String name;Category(String name){this.name=name;}public void remove(String key){changed|=values.remove(name+"."+key)!=null;}}
 public boolean hasChanged(){return changed;}
 public void save(){File dir=file.getParentFile();if(!dir.isDirectory()&&!dir.mkdirs())throw new IllegalStateException("Cannot create config folder");try(FileOutputStream output=new FileOutputStream(file)){values.store(output,"Legacy Console 1.17.1 client settings");changed=false;}catch(IOException e){throw new IllegalStateException("Cannot save console settings",e);}}
}
