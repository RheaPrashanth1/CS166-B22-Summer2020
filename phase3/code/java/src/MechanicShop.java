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

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
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
		
		while(rs.next()){
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
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
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
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
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
	
	public static void AddCustomer(MechanicShop esql){//1
             String first_name, last_name, address, phone_number;
		int id; 


/*Statement stmt = connection.createStatement();
      //Retrieving the data
      
            ResultSet rs = stmt.executeQuery("Show tables");
                  System.out.println("Tables in the current database: ");
                        while(rs.next()) {
                                 System.out.print(rs.getString(1));
                                          System.out.println();
                          }
     
*/



	     while(true) {
            
            System.out.println("Enter customer first name:");
            try{
                    first_name = in.readLine();
                    System.out.println(first_name);
                    

                    //if(first_name.length() > 0){
                    //throw new Exception("Something went wrong.");
		//	break;
                   // }	
//	System.out.println(first_name.length());	
	 if(first_name.length() <= 0){
			throw new Exception("Need to enter something valid: ");
		}	
		 break;   
             


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }      
  	
	   while(true) {

            System.out.println("Enter customer id:");
            try{
                    
		    id = Integer.parseInt(in.readLine());
                    System.out.println(id);
                   
                    break;
                


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }      
        
        while(true) {
            
            System.out.println("Enter customer last name:");
            try{
                    last_name = in.readLine();
                    System.out.println(last_name);
                    if(last_name!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        } 

        while(true) {
            
            System.out.println("Enter customer phone number:");
            try{
                    phone_number = in.readLine();
                    System.out.println(phone_number);
                    if(phone_number!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }
        
        while(true) {
            
            System.out.println("Enter customer address:");
            try{
                    address = in.readLine();
                    System.out.println(address);
                    if(address!=null){ 
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }
			String fname = first_name;
			String lname = last_name;
			String phone = phone_number;
		   
		     /* String query = "INSERT INTO Customer (first_name, last_name, phone_number,address) VALUES (" + first_name + ", \'" + last_name + "\', \'" + phone_number + "\',\'" + address + "\');";
*/
String query = String.format( "INSERT INTO Customer (id, fname, lname, phone, address) Values ( %d,'%s', '%s', '%s', '%s');",id, fname, lname, phone, address);

        	try {	
		esql.executeUpdate(query);
		
		}
		catch (Exception e) {
		System.out.println(e);
		
		}     	
	}
	
	public static void AddMechanic(MechanicShop esql){//2
	//	 int id = 1234;
        String fname = "";
       String lname = "";
       
       
 
       
        while(true) {
            
            System.out.println("Enter Mechanic first name:");
            try{
                    fname = in.readLine();
                    System.out.println(fname);
                    if(fname!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }      
        
        
        while(true) {
            
            System.out.println("Enter mechanic last name:");
            try{
                    lname = in.readLine();
                    System.out.println(lname);
                    if(lname!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        } 
int experience;
int id;

	 while(true) {

            System.out.println("Enter mechanic id:");
            try{
                    id = Integer.parseInt(in.readLine());
                    System.out.println(id);
                    //if(lname!=null){ //(condition)
                    break;
                    //}

                    //}else{
                    //throw new Exception("Something went wrong.");
                   // }


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }

        while(true) {
            
            System.out.println("Enter mechanic years experience:");
            try{
                    experience  = Integer.parseInt(in.readLine());
                    System.out.println(experience);
                    if(experience >= 0 && experience < 100){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }

        

      ///Users/rheaprashanth String query = "INSERT INTO Mechanic (id, fname, lname, experience) VALUES (" + fname + ", \'" + lname + "\', \'" + experience + "\');";
  //int experience = 5;
    String query = String.format( "INSERT INTO Mechanic (id, fname, lname, experience) Values ( %d,'%s', '%s', '%d');",id, fname, lname,experience);
    

	try {
		esql.executeUpdate(query);
		
		}
		catch (Exception e) {
		System.out.println(e);
		
		} 		
	}
	
	public static void AddCar(MechanicShop esql){//3
	    String vin, make, model = "";
	//    int year = 2;

	     
        while(true) {
            
            System.out.println("Enter car VIN");
            try{
                   vin = in.readLine();
                    System.out.println(vin);
                    if(vin!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        } 

        while(true) {
            
            System.out.println("Enter Car Make:");
            try{
                    make = in.readLine();
                    System.out.println(make);
                    if(make!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             

            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }
	int year;        
	while(true) {

            System.out.println("Enter Car Age year:");
            try{
                    year = Integer.parseInt(in.readLine());
                    System.out.println(year);
                    if(year >= 1970){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Year must be greater than 1970)");
                    }

                } catch(Exception e) {
                System.out.println(e);
                continue;
            }
        }

        while(true) {
            
            System.out.println("Enter Car Model:");
            try{
                    model = in.readLine();
                    System.out.println(model);
                    if(model!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }
             
		} catch(Exception e) {
                System.out.println(e);
                continue;
            } 		
	}
	//int year = 1970;
	
	
		String query = String.format( "INSERT INTO Car (vin, make, model, year) Values ('%s','%s', '%s', %d);",vin,make,model,year);
				try {
                esql.executeUpdate(query);

                }
                catch (Exception e) {
                System.out.println(e);

                }

	
      }
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		/*String query = "";
//String number = "9177059239";
		try{
			query = "SELECT C.date, C.comment, C.bill FROM Closed_Request C, Service_Request S  WHERE C.bill < 100 AND C.rid = S.rid; 
                         esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.out.println("Query failed: " + e);
		}
	*/
		String query = " ";
		String fname,phone,address = " ";
//		int id, rid, customer_id, date,odometer;		
		String lname = " ";
		int id;		
		String car_vin, complain = "";
		int rid;
		int customer_id;
		int date;
		int odometer;
		String complaint;
		
		int numPeople = 0;
		String searchLname = "";
		

		try {
		System.out.println("Enter Customer Last Name: ");
		lname = in.readLine();
		searchLname = String.format("SELECT * FROM Customer WHERE lname= '%s'", lname);
		 numPeople = esql.executeQuery(searchLname);
		 System.out.println(numPeople);
		esql.executeQueryAndPrintResult(searchLname);
		//	System.out.println(numPeople);
		}catch(Exception e) {
			System.out.println(e);
			}

//	        System.out.println(numPeople);


		if (numPeople == 1) {
		 
		}



		if (numPeople < 1) {
			System.out.println ("Person is not registered as a customer, please register them: ");;

			             while(true) {

            System.out.println("Enter customer first name:");
            try{
                    fname = in.readLine();
                    //System.out.println(first_name);
                    if(fname!=null){
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }

           while(true) {

            System.out.println("Enter customer id:");
            try{

                    id = Integer.parseInt(in.readLine());
                    System.out.println(id);
                    
                    break;



            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

       }

	 while(true) {

            System.out.println("Enter customer address:");
            try{
                    address = in.readLine();
                    System.out.println(address);
                    if(address!=null){ 
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }

	 while(true) {

            System.out.println("Enter customer phone-number:");
            try{
                    phone = in.readLine();
                    System.out.println(phone);
                    if(phone!=null){ 
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }
		//String lname = "";
	  query = String.format( "INSERT INTO Customer (id, fname, lname, phone, address) Values ( %d,'%s', '%s', '%s', '%s');",id, fname, lname, phone, address);
     

        try {
                esql.executeUpdate(query);
		
                }
                catch (Exception e) {
                System.out.println(e);

                }
			
		
	

		System.out.println("Now you may add Service Information for Customer: ");

		
while(true) {

	System.out.println("Enter car-vin (vin)");
	try{
			car_vin = in.readLine();
			System.out.println(car_vin);
			if(car_vin!=null){ //(condition)
			break;
			}

			else{
			throw new Exception("Something went wrong.");
			}


	} catch(Exception e) {
		System.out.println(e);
		continue;
	}

}

while(true) {

	System.out.println("Enter car complaint");
	try{
		   complaint = in.readLine();
			System.out.println(complaint);
			if(complaint!=null){ //(condition)
			break;
			}

			else{
			throw new Exception("Something went wrong.");
			}


	} catch(Exception e) {
		System.out.println(e);
		continue;
	}

}

while(true) {

	System.out.println("Enter Odometer Reading:");
	try{
			odometer = Integer.parseInt(in.readLine());
			System.out.println(odometer);
			if(odometer >= 0){ //(condition)
			break;
			}

			else{
			throw new Exception("Year must be greater than 0)");
			}

		} catch(Exception e) {
		System.out.println(e);
		continue;
	}
}

while(true) {

	System.out.println("Enter RID:");
	try{
			rid = Integer.parseInt(in.readLine());
			System.out.println(rid);
			
			break;
			}

			
		
		catch(Exception e) {
		System.out.println(e);
		continue;
	}
}

while(true) {

	System.out.println("Enter Service Date:");
	try{
			date = Integer.parseInt(in.readLine());
			System.out.println(date);
			
			break;
			}

			
		catch(Exception e) {
		System.out.println(e);
		continue;
	}
}


//String getCustId = "";
customer_id = id;

int ownership_id;

query = String.format( "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) Values ( %d,%d, '%s', '%s',%d, '%s');",rid,customer_id, car_vin, date, odometer, complain);


        try {
                esql.executeUpdate(query);

                }
                catch (Exception e) {
                System.out.println(e);

                }



while(true) {

        System.out.println("Enter Ownership-id:");
        try{
                        ownership_id = Integer.parseInt(in.readLine());
                        System.out.println(date);

                        break;
                        }


                catch(Exception e) {
                System.out.println(e);
                continue;
        }
}

//customer-id = id;

query = String.format("INSERT INTO Ownership (ownership_id, customer_id, car_vin) Values (%d, %d, '%s');", ownership_id, customer_id, car_vin); 

} //end if statement bracket



		if (numPeople < 1) {


		}
	/*while(true) {

            System.out.println("Enter car-vin (vin)");
            try{
                    car_vin = in.readLine();
                    System.out.println(car_vin);
                    if(car_vin!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }

	while(true) {

            System.out.println("Enter car complaint");
            try{
                   complaint = in.readLine();
                    System.out.println(complaint);
                    if(complaint!=null){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Something went wrong.");
                    }


            } catch(Exception e) {
                System.out.println(e);
                continue;
            }

        }
	
	 //int year;
        while(true) {

            System.out.println("Enter Odometer Reading:");
            try{
                    odometer = Integer.parseInt(in.readLine());
                    System.out.println(odometer);
                    if(odometer >= 0){ //(condition)
                    break;
                    }

                    else{
                    throw new Exception("Year must be greater than 0)");
                    }

                } catch(Exception e) {
                System.out.println(e);
                continue;
            }
        }

//	 int year;
        while(true) {

            System.out.println("Enter Customer Id:");
            try{
                    customer_id = Integer.parseInt(in.readLine());
                    System.out.println(customer_id);
                    //if(year >= 1970){ //(condition)
                    break;
                    }

                    //else{
                    //throw new Exception("Year must be greater than 1970)");
                   // }

                 catch(Exception e) {
                System.out.println(e);
                continue;
            }
        }

	 //int year;
        while(true) {

            System.out.println("Enter RID:");
            try{
                    rid = Integer.parseInt(in.readLine());
                    System.out.println(rid);
                    //if(rid >= 1970){ //(condition)
                    break;
                    }

                    //else{
                    //throw new Exception("Year must be greater than 1970)");
                   // }

                //}
                catch(Exception e) {
                System.out.println(e);
                continue;
            }
        }


	while(true) {

            System.out.println("Enter Service Date:");
            try{
                    date = Integer.parseInt(in.readLine());
                    System.out.println(date);
                    //if(rid >= 1970){ //(condition)
                    //                    break;
                    //                                        }
                    //
                    //                                                            //else{
                    //                                                                                //throw new Exception("Year must be greater than 1970)");
                    //                                                                                                   // }
                    //
                    //                                                                                                                   //}
                    //                                                                                                                                   catch(Exception e) {
                    //                                                                                          
                    //
                    break;
		}
		catch(Exception e) {
 		System.out.println(e);
		continue;
		}
		
	}

		String complain = complaint;
		 query = String.format( "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) Values (%d,%d', '%s', '%s', '%s', '%s');",rid,customer_id,car_vin,date, odometer, complain);
                                try {
                esql.executeUpdate(query);

                }
                catch (Exception e) {
                System.out.println(e);

                }
*/

	}
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		String rid = "";

		String wid = "";		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
	        String query = " ";

		try{
		    //query = " SELECT DISTINCT S.customer_id FROM Closed_Request C, Service_Request S WHERE bill < 100 AND S.rid = C.rid ORDER BY S.customer_id ASC;";

                   //query = "SELECT S.customer_id, C.bill FROM Closed_Request C, Service_Request S WHERE S.rid = C.rid GROUP BY S.customer_id HAVING C.bill < 100;";
		
	            //query = "SELECT CR.date, CR.comment, CR.bill FROM Service_Request SR, Closed_Request CR WHERE SR.rid = CR.rid GROUP BY SR.customer_id HAVING CR.bill < 100;"; 
			query = "SELECT C.fname, C.lname FROM Customer C WHERE C.id IN (SELECT S.customer_id FROM Service_Request S, Closed_Request CR WHERE S.rid = CR.rid GROUP BY S.customer_id HAVING SUM (CR.bill) = 37316);";
	 esql.executeQueryAndPrintResult(query);
		} catch(Exception e) {
		   System.out.println(e);
		}	
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
	
        //gurpuram
	 String query = "";
	 try{
	   System.out.println("Listing first and last name of Customers with more than 20 Cars: ");
	   query = "SELECT fname, lname FROM Customer C WHERE C.id IN (SELECT customer_id FROM Owns GROUP BY customer_id HAVING COUNT(*) > 20);"; 
	esql.executeQueryAndPrintResult(query);
	}catch(Exception e) {
		 System.out.println(e);
	}

/*	
//query = "SELECT C1.make, C1.model FROM Car C1 WHERE C1.vin IN ( SELECT C.car_vin FROM Service_Request S  WHERE S.car_vin = C.vin AND S.odometer < 50000  AND C.year < 1995)";				
	try { 
	System.out.println("Listing all cars built before 1995 having less than 50,000 miles: ");
	query = "SELECT C1.make, C1.model FROM Car C1 WHERE C1.vin IN ( SELECT C.vin FROM Car C,Service_Request S  WHERE S.car_vin = C.vin AND S.odometer < 50000  AND C.year < 1995);"; 
	esql.executeQueryAndPrintResult(query);	 
	}catch(Exception e){
		System.out.println(e);
	}
*/	
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
	String query = "";
	
	try {
        System.out.println("Listing all cars built before 1995 having less than 50,000 miles: ");
        query = "SELECT C1.make, C1.model, C1.year FROM Car C1 WHERE C1.vin IN ( SELECT C.vin FROM Car C,Service_Request S  WHERE C.vin = S.car_vin AND S.odometer < 50000  AND C.year < 1995);";
        esql.executeQueryAndPrintResult(query);
        }catch(Exception e){
                System.out.println(e);
        }
	
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
           		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
	String query = "";
	try{	
	query = "SELECT C.fname, C.lname FROM Customer C WHERE C.id IN SELECT S.customer_id, SUM(bill) FROM Service_Request S, Closed_Request CR WHERE S.rid = CR.rid GROUP BY S.customer_id ORDER BY SUM(bill) DESC;";

	//query = "SELECT S.rid FROM Service_Request S WHERE S.customer_id = '123';";
esql.executeQueryAndPrintResult(query);
	}	
catch(Exception e){
                System.out.println(e);
        }
	}
	
}
