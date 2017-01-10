package agency.data;

import java.util.List;

/**
 * Utility interface used to organize data for output.  Primary functions are
 * to inform writers (i.e., primarily agency.data.DataOutput) of header names
 * as weill as provide a definite order for data.  Should typically be used
 * by Agency internally, and often with
 * {@link agency.data.DefaultDataObjectManager}.
 *
 */
public interface AgencyData {

List<String> getHeaders();
List<Object> getValues();

}
