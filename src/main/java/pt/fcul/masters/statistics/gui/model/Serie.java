package pt.fcul.masters.statistics.gui.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@lombok.Data
public class Serie<X,Y> {

	private List<Data<X,Y>> data = new LinkedList<>();
	private String name = "values";

	public Serie(Collection<? extends X> xs, Collection<? extends Y> ys){
		if(xs.size() != ys.size())
			throw new IllegalArgumentException("Collections must have the same size");
		Iterator<? extends X> xIterator = xs.iterator();
		Iterator<? extends Y> yIterator = ys.iterator();
		while(xIterator.hasNext() && yIterator.hasNext()) 
			add(xIterator.next(),yIterator.next());
	}

	public Serie(Map<? extends X,? extends Y> data){
		data.forEach(this::add);
	}

	public static Serie<?,?> of(Map<?,?> data){
		return new Serie<>(data);
	}
	
	public static Serie<?,?> of(Collection<?> xs, Collection<? > ys){
		return new Serie<>(xs,ys);
	}
	
	
	public boolean add(X x, Y y) {
		return data.add(new Data<X,Y>(x,y));
	}

	public String toJSONObject() {
		JSONObject jsobject = new JSONObject();
		jsobject.put("name", name);
		jsobject.put("data", seriesToJSONArray(data));
		return jsobject.toString();
	}
	
	
	
	public JSONArray seriesToJSONArray(List<Data<X,Y>> list) {
		JSONArray jsonArray = new JSONArray();
	    for(Data<X,Y> data : list) {
	    	JSONObject dataPoint = new JSONObject();
	    	dataPoint.put("x", data.x());
	    	dataPoint.put("y", data.y());
	        jsonArray.put(dataPoint);
	    }
	    return jsonArray;
	}


	public record Data<X,Y> (X x, Y y) {}
}
