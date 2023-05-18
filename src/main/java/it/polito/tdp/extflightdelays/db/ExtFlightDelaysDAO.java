package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.CoppiaA;
import it.polito.tdp.extflightdelays.model.Flight;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> loadAllAirports() {
		String sql = "SELECT * FROM airports";
		List<Airport> result = new ArrayList<Airport>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				result.add(airport);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT * FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("ID"), rs.getInt("AIRLINE_ID"), rs.getInt("FLIGHT_NUMBER"),
						rs.getString("TAIL_NUMBER"), rs.getInt("ORIGIN_AIRPORT_ID"),
						rs.getInt("DESTINATION_AIRPORT_ID"),
						rs.getTimestamp("SCHEDULED_DEPARTURE_DATE").toLocalDateTime(), rs.getDouble("DEPARTURE_DELAY"),
						rs.getDouble("ELAPSED_TIME"), rs.getInt("DISTANCE"),
						rs.getTimestamp("ARRIVAL_DATE").toLocalDateTime(), rs.getDouble("ARRIVAL_DELAY"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	/*
	 * Metodo che calcola tutte le tratte tra un aeroporto e un altro, con anche la distanza media;
	 * il problema è che le tratte sono doppie, perchè i voli hanno due versi.
	 */
	public List<CoppiaA> loadTratteDistanza(double distanzaMedia, Map<Integer, Airport> idMap) {
		String sql = "SELECT ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID, AVG(DISTANCE) AS media "
				+ "FROM flights "
				+ "GROUP BY ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID "
				+ "HAVING AVG(DISTANCE) >= ?";
		List<CoppiaA> coppie = new ArrayList<CoppiaA>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setDouble(1, distanzaMedia);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport origine = idMap.get(rs.getInt(1));
				Airport destinazione = idMap.get(rs.getInt(2));
				double distanza = rs.getDouble("media");
				
				if(origine != null && destinazione != null) {
					CoppiaA c = new CoppiaA(origine, destinazione, distanza);
					coppie.add(c);
				}
			}

			conn.close();
			return coppie;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
		
	}
	
	public List<Airport> getAirportDist(double dist, Map<Integer, Airport> idMap){
		String sql = "SELECT distinct a.ID "
				+ "FROM airports a, "
				+ "(SELECT ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID, AVG(DISTANCE) AS media "
				+ "FROM flights "
				+ "GROUP BY ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID "
				+ "HAVING AVG(DISTANCE) >= ?) tratte "
				+ "WHERE a.id = ORIGIN_AIRPORT_ID OR a.id = DESTINATION_AIRPORT_ID";
		List<Airport> ret = new ArrayList<Airport>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setDouble(1, dist);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				ret.add(idMap.get(rs.getInt(1)));
			}

			conn.close();
			return ret;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
}
