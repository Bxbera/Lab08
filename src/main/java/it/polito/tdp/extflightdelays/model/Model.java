package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	ExtFlightDelaysDAO dao;
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	//List<Airport> aeroporti;
	Map<Integer, Airport> idMap;
	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer, Airport>();
		for(Airport a: dao.loadAllAirports()) {
			idMap.put(a.getId(), a);
		}
	}
	
	public void creaGrafo(double distanzaMedia) {
		
		grafo = new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		// inserisco i vertici solo dopo aver preso i dati che mi servono, cioè quelli che sono sopra la distanza media 
		Graphs.addAllVertices(grafo, dao.getAirportDist(distanzaMedia, idMap));
		
		// mi da la lista di tutte le tratte che soddisfano la condizione di distanza media
		List<CoppiaA> coppie = dao.loadTratteDistanza(distanzaMedia, idMap);
		
		// dalle tratte devo creare gli archi e aggiungergli il peso, facendo la media tra andata e ritorno nel caso ci sia già l'arco
		for(CoppiaA c : coppie) {
			Airport origine = c.getPartenza();
			Airport dest = c.getDestinazione();
			double media = c.getDistanzaMedia();
			if(grafo.containsVertex(origine) && 
					grafo.containsVertex(dest)) {
				DefaultWeightedEdge edge = grafo.getEdge(origine, dest);
				if(edge==null) {
					edge = grafo.addEdge(origine, dest);
					grafo.setEdgeWeight(edge, media); 
				} else {
					double altra = grafo.getEdgeWeight(edge);
					media = (media+altra)/2;
					grafo.setEdgeWeight(edge, media);
				}
			}
		}
		//System.out.println("numeor di vertici: "+grafo.vertexSet().size()+"\n");
		//System.out.println("numeor di archi: "+grafo.edgeSet().size());
		
	}

	public int getNumeroVertici() {
		int num = 0;
		if(grafo!=null) 
			num = grafo.vertexSet().size();  
		return num;
	}
	
	public int getNumeroArchi() {
		int num = 0;
		if(grafo!=null) {	
			num = grafo.edgeSet().size();
		}
		return num;
	}
	
	public Set<DefaultWeightedEdge> getEdges(){
		if(grafo==null) {
			return null;
		} else {
			return grafo.edgeSet();
		}
	}
	
	public List<CoppiaA> getTratte(){
		List<CoppiaA> ret = new ArrayList<>();
		if(grafo!=null) {
			for(DefaultWeightedEdge dfe : this.grafo.edgeSet()) {
				Airport a1 = this.grafo.getEdgeSource(dfe);
				Airport a2 = Graphs.getOppositeVertex(this.grafo, dfe, a1);
				double peso = this.grafo.getEdgeWeight(dfe);
				ret.add(new CoppiaA(a1, a2, peso));
			}
		}
			
		return ret;
	}
	/*
	private List<Airport> dammiVertici(double distanzaMedia) {
		Map<Integer, Airport> ret = new HashMap<>();
		List<CoppiaA> coppie = dao.loadTratteDistanza(distanzaMedia, idMap);
		for(CoppiaA c : coppie) {
			Airport origine = c.getPartenza();
			Airport dest = c.getDestinazione();
			if(!ret.containsKey(origine.getId()))
				ret.put(origine.getId(), origine);
			if(!ret.containsKey(dest.getId()))
				ret.put(dest.getId(), dest);
		}
		return null;
	}

	
	private List<CoppiaA> dammiTratteValide(double distanzaMedia) {
		List<CoppiaA> ret = new ArrayList<>();
		for(CoppiaA c : dao.loadAllTratte(idMap)) {
			if(c.getDistanzaMedia()>=distanzaMedia) {
				ret.add(c);
			}
		}
		return ret;
	}
	*/
}
