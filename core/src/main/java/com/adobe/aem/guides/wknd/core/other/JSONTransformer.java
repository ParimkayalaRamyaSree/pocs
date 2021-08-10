import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

public final class JSONTransformer {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(JSONTransformer.class);

	/** The mapper. */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Private constructor to prevent instantiation of a new JSON transformer.
	 */
	private JSONTransformer() {
		// Hide default constructor
	}

	/*
	 * This block is for setting the returning the empty values for null model parameters
	 */
	static {
		mapper.setSerializerProvider(new EmptySerializerProvider()).enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
		        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	/**
	 * This method is used to marshall the java object to json string.
	 *
	 * @param object the object
	 * @return the string
	 */
	public static String convertToJson(Object object) {
		LOG.debug("Start of method convertToJson()");

		String jsonString = StringUtils.EMPTY;
		if (null != object) {
			try {
				jsonString = mapper.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				LOG.error("Invalid JSON format ", e);
			}
		}
		LOG.debug("End of method convertToJson()");
		return jsonString;
	}

	/**
	 * The Class EmptySerializerProvider.
	 */
	private static class EmptySerializerProvider extends DefaultSerializerProvider {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 8053649209925053781L;

		/**
		 * Instantiates a new empty serializer provider.
		 */
		public EmptySerializerProvider() {
			super();
		}

		/**
		 * Instantiates a new empty serializer provider.
		 *
		 * @param src the src
		 */
		public EmptySerializerProvider(EmptySerializerProvider src) {
			super(src);
		}

		/**
		 * Instantiates a new empty serializer provider.
		 *
		 * @param src the src
		 * @param config the config
		 * @param f the f
		 */
		protected EmptySerializerProvider(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
			super(src, config, f);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.DefaultSerializerProvider#copy()
		 */
		@Override
		public DefaultSerializerProvider copy() {
			if (getClass() != EmptySerializerProvider.class) {
				return super.copy();
			}
			return new EmptySerializerProvider(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.DefaultSerializerProvider#
		 * createInstance(com.fasterxml.jackson.databind.SerializationConfig,
		 * com.fasterxml.jackson.databind.ser.SerializerFactory)
		 */
		@Override
		public EmptySerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
			return new EmptySerializerProvider(this, config, jsf);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.SerializerProvider#
		 * findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		public JsonSerializer<Object> findNullValueSerializer(BeanProperty property) throws JsonMappingException {
			final Class<?> propertyClass = property.getType().getRawClass();
			if (propertyClass.isAssignableFrom(Collection.class) || propertyClass.isArray()) {
				return new JsonSerializer<Object>() {
					@Override
					public void serialize(Object arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
						arg1.writeStartArray();
						arg1.writeEndArray();
					}
				};
			} else if (propertyClass.isAssignableFrom(String.class) || propertyClass.isAssignableFrom(Number.class)) {
				return new JsonSerializer<Object>() {
					@Override
					public void serialize(Object arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
						arg1.writeString(StringUtils.EMPTY);
					}
				};
			} else {
				return new JsonSerializer<Object>() {
					@Override
					public void serialize(Object arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
						arg1.writeStartObject();
						arg1.writeEndObject();
					}
				};
			}
		}

	}

	/**
	 * This method is used to unmarshall an array of json strings to a List of Map.
	 *
	 * @param multi an array of json strings
	 * @return a List of Map objects unmarshalled from the json string array
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Map<String, Object>> getJSONObject(String[] multi) {
		List<Map<String, Object>> output = null;
		final List<Map> jsonMapList = getJSONObject(multi, Map.class);
		if (jsonMapList != null) {
			output = new ArrayList<>();
			for (final Map<String, Object> map : jsonMapList) {
				output.add(map);
			}
		}
		return output;
	}

	/**
	 * This method is used to unmarshall an array of json strings to a List of specified type.
	 *
	 * @param <T> the generic type
	 * @param multi an array of json strings
	 * @param objectType a type
	 * @return a List of specified type objects unmarshalled from the json string array
	 */
	public static <T> List<T> getJSONObject(String[] multi, Class<T> objectType) {
		LOG.debug("Start of method getJSONObject()");
		List<T> output = null;
		if (multi != null) {
			output = new ArrayList<>();
			T jsonObj;
			for (final String json : multi) {
				jsonObj = getJSONObject(json, objectType);
				if (jsonObj != null) {
					output.add(jsonObj);
				}
			}
		}
		LOG.debug("End of method getJSONObject()");
		return output;
	}

	/**
	 * This Method is used to unmarshall the object to specific model class.
	 *
	 * @param <T> the generic type
	 * @param jsonString the json string
	 * @param objectClass the object class
	 * @return the JSON object
	 */
	public static <T> T getJSONObject(String jsonString, Class<T> objectClass) {
		LOG.debug("Start of method getJSONObject()");
		try {
			return mapper.readValue(jsonString, objectClass);
		} catch (IOException e) {
			LOG.error("IOException : ", e);
		}
		LOG.debug("End of method getJSONObject()");
		return null;
	}

}
