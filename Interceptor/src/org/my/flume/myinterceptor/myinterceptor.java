package org.my.flume.myinterceptor;


import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.interceptor.RegexExtractorInterceptorPassThroughSerializer;
import org.apache.flume.interceptor.RegexExtractorInterceptorSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;


public class myinterceptor implements Interceptor {

  static final String REGEX = "regex";
  static final String SERIALIZERS = "serializers";

  private static final Logger logger = LoggerFactory
      .getLogger(myinterceptor.class);

  private final Pattern regex;
  private final List<NameAndSerializer> serializers;

  private myinterceptor(Pattern regex,
      List<NameAndSerializer> serializers) {
    this.regex = regex;
    this.serializers = serializers;
  }

  public void initialize() {
    // NO-OP...
  }

  public void close() {
    // NO-OP...
  }

  public Event intercept(Event event) {
    Matcher matcher = regex.matcher(
        new String(event.getBody(), Charsets.UTF_8));
    Map<String, String> headers = event.getHeaders();
    if (matcher.find()) {
      for (int group = 0, count = matcher.groupCount(); group < count; group++) {
        int groupIndex = group + 1;
        if (groupIndex > serializers.size()) {
          if (logger.isDebugEnabled()) {
            logger.debug("Skipping group {} to {} due to missing serializer",
                group, count);
          }
          break;
        }
        NameAndSerializer serializer = serializers.get(group);
        if (logger.isDebugEnabled()) {
          logger.debug("Serializing {} using {}", serializer.headerName,
              serializer.serializer);
        }
        headers.put(serializer.headerName,
            serializer.serializer.serialize(matcher.group(groupIndex)));
      }
    }
    return event;
  }

  public List<Event> intercept(List<Event> events) {
    List<Event> intercepted = Lists.newArrayListWithCapacity(events.size());
    int addnum=0;
    for(int i=0;i<events.size();i++){
    	Event interceptedEvent=null;
    	Matcher matcher=regex.matcher(
    			new String(events.get(i).getBody(), Charsets.UTF_8));
    	if(matcher.find()){
    		interceptedEvent = intercept(events.get(i));
			  intercepted.add(interceptedEvent);
			  addnum=i;
    	}else{
    		 String body=new String(intercepted.get(addnum).getBody(), Charsets.UTF_8)+"\n"+new String(events.get(i).getBody(), Charsets.UTF_8);
	    	 intercepted.get(addnum).setBody(body.getBytes());
    	}
    }
    return intercepted;
  }

  public static class Builder implements Interceptor.Builder {

    private Pattern regex;
    private List<NameAndSerializer> serializerList;
    private final RegexExtractorInterceptorSerializer defaultSerializer = new RegexExtractorInterceptorPassThroughSerializer();

    public void configure(Context context) {
      String regexString = context.getString(REGEX);
      Preconditions.checkArgument(!StringUtils.isEmpty(regexString),
          "Must supply a valid regex string");
      regex = Pattern.compile(regexString);
      regex.pattern();
      regex.matcher("").groupCount();
      configureSerializers(context);
    }

    private void configureSerializers(Context context) {
      String serializerListStr = context.getString(SERIALIZERS);
      Preconditions.checkArgument(!StringUtils.isEmpty(serializerListStr),
          "Must supply at least one name and serializer");

      String[] serializerNames = serializerListStr.split("\\s+");

      Context serializerContexts =
          new Context(context.getSubProperties(SERIALIZERS + "."));

      serializerList = Lists.newArrayListWithCapacity(serializerNames.length);
      for(String serializerName : serializerNames) {
        Context serializerContext = new Context(
            serializerContexts.getSubProperties(serializerName + "."));
        String type = serializerContext.getString("type", "DEFAULT");
        String name = serializerContext.getString("name");
        Preconditions.checkArgument(!StringUtils.isEmpty(name),
            "Supplied name cannot be empty.");

        if("DEFAULT".equals(type)) {
          serializerList.add(new NameAndSerializer(name, defaultSerializer));
        } else {
          serializerList.add(new NameAndSerializer(name, getCustomSerializer(
              type, serializerContext)));
        }
      }
    }

    private RegexExtractorInterceptorSerializer getCustomSerializer(
        String clazzName, Context context) {
      try {
        RegexExtractorInterceptorSerializer serializer = (RegexExtractorInterceptorSerializer) Class
            .forName(clazzName).newInstance();
        serializer.configure(context);
        return serializer;
      } catch (Exception e) {
        logger.error("Could not instantiate event serializer.", e);
        Throwables.propagate(e);
      }
      return defaultSerializer;
    }

    public Interceptor build() {
      Preconditions.checkArgument(regex != null,
          "Regex pattern was misconfigured");
      Preconditions.checkArgument(serializerList.size() > 0,
          "Must supply a valid group match id list");
      return new myinterceptor(regex, serializerList);
    }
  }

  static class NameAndSerializer {
    private final String headerName;
    private final RegexExtractorInterceptorSerializer serializer;

    public NameAndSerializer(String headerName,
        RegexExtractorInterceptorSerializer serializer) {
      this.headerName = headerName;
      this.serializer = serializer;
    }
  }
}
