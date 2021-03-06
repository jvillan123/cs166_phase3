/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	
	
	public static void AddPlane(DBproject esql) {//1
		try{
			String add_plane = "INSERT INTO Plane(make, model, age, seats) VALUES( ";
			System.out.print("\tEnter Make: $");
			String plane_make = in.readLine();
			System.out.print("\tEnter Modle: $");
			String plane_model = in.readLine();
			System.out.print("\tEnter Age (YYYY): $");
			String plane_age = in.readLine();
			System.out.print("\tEnter seats: $");
			String plane_seats = in.readLine();
		
			add_plane = add_plane  + ("'" + plane_make + "'") + ", " + ("'" + plane_model + "'") + ", " + plane_age + ", " + plane_seats + ");" ;   
			// System.out.println(add_plane);
			esql.executeUpdate(add_plane);
			
			System.out.print("New plane added successfully\n");
			
			
		}catch(Exception e){
         System.err.println (e.getMessage());
      } 
			
		
	}

	public static void AddPilot(DBproject esql) {//2
		try{
			// errors out stating that it need more info -> may be a problem with triggers
			String add_pilot = "INSERT INTO Pilot(fullname, nationality) VALUES(";
			
			System.out.print("\tEnter Name: $");
			String pilot_name = in.readLine();
			System.out.print("\tEnter Nationality: $");
			String pilot_nation = in.readLine();
			
			add_pilot = add_pilot + ("'" + pilot_name + "', ") + ("'" + pilot_nation + "');");
			System.out.println(add_pilot);
			esql.executeUpdate(add_pilot);
			
			System.out.print("New pilot added successfully\n");
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		try{
			
			String add_flight = "INSERT INTO Flight(cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) VALUES( ";
			//make assumption that num_sold is 0 at this point
			//make assumption that cost is user decision
			//make assumption that actual arrival and departure date is correct and in format YYYY-MM-DDD
			//for flight portion
			System.out.print("\tEnter flight cost: $");
			String flight_cost = in.readLine();
			System.out.print("\tEnter number of stops : $");
			String flight_stops = in.readLine();
			System.out.print("\tEnter arrival airport code (ex. JFK): $");
			String arrival_airport = in.readLine();
			System.out.print("\tEnter departure airport code (ex. JFK): $");
			String departure_airport = in.readLine();
			System.out.print("\tEnter departure date: (YYYY-MM-DD): $");
			String actual_departure_date = in.readLine();
			System.out.print("\tEnter arrivale date: (YYYY-MM-DD): $");
			String actual_arrival_date = in.readLine();
			
			add_flight = add_flight + flight_cost + ", " + 0 + "," + flight_stops + 
						", '" + actual_departure_date	+ "', '"  + actual_arrival_date	+ 
						"', '" + arrival_airport + "', '" + departure_airport + "');";
			// System.out.print(add_flight);
			esql.executeUpdate(add_flight);

			//get flight number for schdeduling
			String query_flight_num = "SELECT F.fnum FROM Flight F WHERE F.cost=" + flight_cost + 
						" AND F.num_sold=" + 0 + " AND F.num_stops=" + flight_stops + 
						" AND F.actual_departure_date='" + actual_departure_date + "'" + 
						" AND F.actual_arrival_date='" + actual_arrival_date + "'" +
						" AND F.arrival_airport='" + arrival_airport + "'" +
						" AND F.departure_airport='" + departure_airport + "';";
			List<List<String>> flight_num_query_result = esql.executeQueryAndReturnResult(query_flight_num);
			String flight_num = flight_num_query_result.get(0).get(0);

			//for flight info portion
			String add_flight_info = "INSERT INTO FlightInfo(flight_id, pilot_id, plane_id) VALUES( ";
			System.out.print("\tEnter Pilot ID: $");
			String pilot_id = in.readLine();
			System.out.print("\tEnter Plane ID: $");
			String plane_id = in.readLine();
			add_flight_info = add_flight_info + flight_num + ", " + pilot_id + ", " + plane_id + ");";
			System.out.print(add_flight_info);
			esql.executeUpdate(add_flight_info);
			

			//for scheduling portion
			String add_schedule = "INSERT INTO Schedule(flightNum, departure_time, arrival_time) VALUES( ";
			add_schedule = add_schedule + flight_num + ", '" + actual_departure_date + "', '" + actual_arrival_date + "');";
			//System.out.print(add_schedule);
			esql.executeUpdate(add_schedule);
			
			System.out.print("New Flight added successfully\n");
			



		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}

	public static void AddTechnician(DBproject esql) {//4
		try{
			// errors out stating that it need more info -> may be a problem with triggers
			String add_tech = "INSERT INTO Technician(full_name) VALUES( ";
			System.out.print("\tEnter Name: $");
			String tech_name = in.readLine();
			
			add_tech = add_tech + ("'" + tech_name + "');");
			System.out.print(add_tech);
			esql.executeUpdate(add_tech);
			
			System.out.print("New Technician added successfully\n");
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		boolean debug = false;
		try{
			
			//add reservation part
			String add_reservation = "INSERT INTO Reservation(cid, fid, status) VALUES( ";
			
			System.out.print("\tEnter Flight Number: $");
			String flight_id = in.readLine();
			// System.out.print("\tEnter Flight Departure Date (YYYY-MM-DD): $");
			// String date_of_flight = in.readLine();
			System.out.print("\tEnter Customer id: $");
			String customer_id = in.readLine();
			
			String flight_schedual_info = "SELECT x.plane_id FROM FlightInfo x, Flight y, Schedule z WHERE x.flight_id = ";
			
			// flight_schedual_info = flight_schedual_info + flight_id + " AND y.fnum = x.flight_id AND z.flightNum = x.flight_id AND z.departure_time='" + date_of_flight + "';";
			flight_schedual_info = flight_schedual_info + flight_id + " AND y.fnum = x.flight_id AND z.flightNum = x.flight_id;";
			if(debug == true){
				System.out.println(flight_schedual_info);
			}
			List<List<String>> planeinfo = esql.executeQueryAndReturnResult(flight_schedual_info);
			if(debug == true){
				System.out.println("Going to output planeinfo");
				for(List<String> item_list : planeinfo){
					System.out.println("New Item List");
					for(String item : item_list){
						System.out.println("item in planeinfo[item_list]: " + item);
					}
				}
				System.out.println("Completed outputting planeinfo");
			}

			List<String> plane = planeinfo.get(0);
			String plane_id = plane.get(0);
			
			String plane_seats_querry = "SELECT seats FROM Plane x WHERE x.id = " + plane_id + ";";
			if(debug == true){
				System.out.println(plane_seats_querry);
			}
			List<List<String>> planeseats = esql.executeQueryAndReturnResult(plane_seats_querry);
			List<String> plane_seats_num_row = planeseats.get(0);
			
			int num_of_total_seats = Integer.parseInt(plane_seats_num_row.get(0));
			
			// String flight_seats_sold_querry = "SELECT num_sold FROM Flight x WHERE x.fnum = " + flight_id + " AND x.actual_departure_date='" + date_of_flight + "';";
			String flight_seats_sold_querry = "SELECT num_sold FROM Flight x WHERE x.fnum = " + flight_id + ";";
			if(debug == true){
				System.out.println(flight_seats_sold_querry);
			}
			List<List<String>> flightseats = esql.executeQueryAndReturnResult(flight_seats_sold_querry);
			List<String> Flight_seats_row = flightseats.get(0);
			
			int num_of_seats_sold = Integer.parseInt(Flight_seats_row.get(0));
			
			int available_seats = num_of_total_seats - num_of_seats_sold;
			
			String customer_exist_query = "SELECT c.id FROM Customer c WHERE c.id = " + customer_id ;
			if(debug == true){
				System.out.println(customer_exist_query);
			}
			List<List<String>> customerinfo = esql.executeQueryAndReturnResult(customer_exist_query);
			
			add_reservation = add_reservation + customerinfo.get(0).get(0) + ", " + flight_id + ", ";
			if(available_seats > 0){
				add_reservation = add_reservation + "'R');";
			}else{
				add_reservation = add_reservation + "'W');";
			}
			if(debug == true){
				System.out.println(add_reservation);
			}
			esql.executeUpdate(add_reservation);

			//update flight.num_sold part
			String update_flight_num_sold = "UPDATE Flight SET num_sold=" + (num_of_seats_sold + 1) + " WHERE fnum=" + flight_id + ";";
			esql.executeUpdate(update_flight_num_sold);
			
			System.out.print("New Reservation added successfully\n");
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		try{
			
			
			System.out.print("\tEnter Flight Number: $");
			String flight_num = in.readLine();
			System.out.print("\tEnter Date: $");
			String date_of_flight = in.readLine();
			
			String flight_schedual_info = "SELECT x.plane_id FROM FlightInfo x, Flight y, Schedule z WHERE x.flight_id =";
			
			flight_schedual_info = flight_schedual_info + flight_num + " AND y.fnum = x.flight_id AND z.flightNum = x.flight_id;";
			
			List<List<String>> planeinfo = esql.executeQueryAndReturnResult(flight_schedual_info);
			List<String> plane = planeinfo.get(0);
			String plane_id = plane.get(0);
			
			String plane_seats_querry = "SELECT x.seats FROM Plane x WHERE x.id = " + plane_id ;
			List<List<String>> planeseats = esql.executeQueryAndReturnResult(plane_seats_querry);
			List<String> plane_seats_num_row = planeseats.get(0);
			
			int num_of_total_seats = Integer.parseInt(plane_seats_num_row.get(0));
			
			String flight_seats_sold_querry = "SELECT x.num_sold FROM Flight x WHERE x.fnum = " + flight_num + ";";
			List<List<String>> flightseats = esql.executeQueryAndReturnResult(flight_seats_sold_querry);
			List<String> Flight_seats_row = flightseats.get(0);
			
			int num_of_seats_sold = Integer.parseInt(Flight_seats_row.get(0));
			
			int available_seats = num_of_total_seats - num_of_seats_sold;
			
			System.out.println("\tFlight Number " + flight_num + " has "+ available_seats +" available seats");
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		try{
			
			
			String query = "SELECT plane_id, count(rid) FROM Repairs GROUP BY plane_id ORDER BY count(rid) DESC ";
			esql.executeQueryAndPrintResult(query);
			
			System.out.print("Press Enter to continue\n");
			String con = in.readLine();
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		try{
			
			String query = "SELECT EXTRACT(YEAR FROM repair_date), count(rid) FROM Repairs GROUP BY EXTRACT(YEAR FROM repair_date) ORDER BY count(rid) ASC ";
			esql.executeQueryAndPrintResult(query);
			System.out.print("Press Enter to continue\n");
			String con = in.readLine();
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		try{
			
			boolean debug = false;
			System.out.print("\tEnter flight number: $");
			String flight_num = in.readLine();
			System.out.print("\tEnter passenger status(W,C,R): $");
			String status_wanted = in.readLine();
			
			String query = "SELECT count(cid) FROM Reservation WHERE status = '";
			
			query = query + status_wanted + "' AND fid = '" + flight_num + "';";
			if(debug == true){
				System.out.println(query);
			}
			esql.executeQueryAndPrintResult(query);
			
			System.out.print("Press Enter to continue\n");
			String con = in.readLine();
			
			
		} catch(Exception e){
         System.err.println (e.getMessage());
      } 
	}
}

