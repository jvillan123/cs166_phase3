DROP TABLE IF EXISTS Customer CASCADE;--OK
DROP TABLE IF EXISTS Flight CASCADE;--OK
DROP TABLE IF EXISTS Pilot CASCADE;--OK
DROP TABLE IF EXISTS Plane CASCADE;--OK
DROP TABLE IF EXISTS Technician CASCADE;--OK

DROP TABLE IF EXISTS Reservation CASCADE;--OK
DROP TABLE IF EXISTS FlightInfo CASCADE;--OK
DROP TABLE IF EXISTS Repairs CASCADE;--OK
DROP TABLE IF EXISTS Schedule CASCADE;--OK

CREATE SEQUENCE pilot_id_gen START WITH 0;
CREATE SEQUENCE customer_id_gen START WITH 0;
CREATE SEQUENCE flightNum_gen START WITH 0;
CREATE SEQUENCE plane_id_gen START WITH 0;
CREATE SEQUENCE tech_id_gen START WITH 0;
CREATE SEQUENCE reservation_rnum_gen START WITH 0;
CREATE SEQUENCE repair_rid_gen START WITH 0;
CREATE SEQUENCE flightinfo_fiid_gen START WITH 0;
CREATE SEQUENCE schedule_id_gen START WITH 0;

CREATE OR REPLACE FUNCTION get_plane_id()
	RETURNS "trigger" AS
	$BODY$
	BEGIN
		perform setval('plane_id_gen', (SELECT MAX(id) FROM Plane) );
		NEW.id := nextval('plane_id_gen');
		RETURN NEW;
	END;
	$BODY$
	LANGUAGE plgsql VOLATILE;

CREATE OR REPLACE FUNCTION get_pilot_id()
	RETURNS "trigger" AS
	$BODY$
	BEGIN
		perform setval('pilot_id_gen', (SELECT MAX(id) FROM Pilot) );
		NEW.id := nextval('pilot_id_gen');
		RETURN NEW;
	END;
	$BODY$
	LANGUAGE plgsql VOLATILE;
	
CREATE OR REPLACE FUNCTION get_customer_id()
	RETURNS "trigger" AS
	$BODY$
	BEGIN
		perform setval('customer_id_gen', (SELECT MAX(id) FROM Customer) );
		NEW.id := nextval('customer_id_gen');
		RETURN NEW;
	END;
	$BODY$
	LANGUAGE plgsql VOLATILE;
	
CREATE OR REPLACE FUNCTION get_tech_id()
	RETURNS "trigger" AS
	$BODY$
	BEGIN
		perform setval('tech_id_gen', (SELECT MAX(id) FROM Technician) );
		NEW.id := nextval('tech_id_gen');
		RETURN NEW;
	END;
	$BODY$
	LANGUAGE plgsql VOLATILE;

CREATE OR REPLACE FUNCTION get_reservation_rnum()
	RETURNS "trigger" AS
	
	$BODY$
		perform setval('reservation_rnum_gen', (SELECT MAX(rnum) FROM Reservation );
		NEW.rnum := nextval('reservation_rnum_gen');
		RETURN NEW;
	end;
	$BODY$
	LANGUAGE plgsql VOLATILE;
	
CREATE OR REPLACE FUNCTION get_flightinfo_fiid()
	RETURNS "trigger" AS
	
	$BODY$
		perform setval('flightinfo_fiid_gen', (SELECT MAX(fiid) FROM FlightInfo );
		NEW.fiid := nextval('flightinfo_fiid_gen');
		RETURN NEW;
	end;
	$BODY$
	LANGUAGE plgsql VOLATILE;

CREATE OR REPLACE FUNCTION get_repairs_rid()
	RETURNS "trigger" AS
	
	$BODY$
		perform setval('repair_rid_gen', (SELECT MAX(rid) FROM Repairs );
		NEW.rid := nextval('repair_rid_gen');
		RETURN NEW;
	end;
	$BODY$
	LANGUAGE plgsql VOLATILE;

CREATE OR REPLACE FUNCTION get_schedule_id()
	RETURNS "trigger" AS
	
	$BODY$
		perform setval('schedule_id_gen', (SELECT MAX(id) FROM schedule);
		NEW.rnum := nextval('schedule_id_gen');
		RETURN NEW;
	end;
	$BODY$
	LANGUAGE plgsql VOLATILE;

CREATE OR REPLACE FUNCTION get_flight_fnum()
	RETURNS "trigger" AS
	$BODY$
	BEGIN
		perform setval('flightNum_gen', (SELECT MAX(fnum) FROM Flight) );
		NEW.id := nextval('flightNum_gen');
		RETURN NEW;
	END;
	$BODY$
	LANGUAGE plgsql VOLATILE;

-------------
---DOMAINS---
-------------
CREATE DOMAIN us_postal_code AS TEXT CHECK(VALUE ~ '^\d{5}$' OR VALUE ~ '^\d{5}-\d{4}$');
CREATE DOMAIN _STATUS CHAR(1) CHECK (value IN ( 'W' , 'C', 'R' ) );
CREATE DOMAIN _GENDER CHAR(1) CHECK (value IN ( 'F' , 'M' ) );
CREATE DOMAIN _CODE CHAR(2) CHECK (value IN ( 'MJ' , 'MN', 'SV' ) ); --Major, Minimum, Service
CREATE DOMAIN _PINTEGER AS int4 CHECK(VALUE > 0);
CREATE DOMAIN _PZEROINTEGER AS int4 CHECK(VALUE >= 0);
CREATE DOMAIN _YEAR_1970 AS int4 CHECK(VALUE >= 0);
CREATE DOMAIN _SEATS AS int4 CHECK(VALUE > 0 AND VALUE < 500);--Plane Seats

------------
---TABLES---
------------
CREATE TABLE Customer
(
	id INTEGER NOT NULL,
	fname CHAR(24) NOT NULL,
	lname CHAR(24) NOT NULL,
	gtype _GENDER NOT NULL,
	dob DATE NOT NULL,
	address CHAR(256),
	phone CHAR(10),
	zipcode char(10),
	PRIMARY KEY (id)
);

CREATE TABLE Pilot
(
	id INTEGER NOT NULL,
	fullname CHAR(128),
	nationality CHAR(24),
	PRIMARY KEY (id)
);

CREATE TABLE Flight
(
	fnum INTEGER NOT NULL,
	cost _PINTEGER NOT NULL,
	num_sold _PZEROINTEGER NOT NULL,
	num_stops _PZEROINTEGER NOT NULL,
	actual_departure_date DATE NOT NULL,
	actual_arrival_date DATE NOT NULL,
	arrival_airport CHAR(5) NOT NULL,-- AIRPORT CODE --
	departure_airport CHAR(5) NOT NULL,-- AIRPORT CODE --
	PRIMARY KEY (fnum)
);

CREATE TABLE Plane
(
	id INTEGER NOT NULL,
	make CHAR(32) NOT NULL,
	model CHAR(64) NOT NULL,
	age _YEAR_1970 NOT NULL,
	seats _SEATS NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE Technician
(
	id INTEGER NOT NULL,
	full_name CHAR(128) NOT NULL,
	PRIMARY KEY (id)
);

---------------
---RELATIONS---
---------------

CREATE TABLE Reservation
(
	rnum INTEGER NOT NULL,
	cid INTEGER NOT NULL,
	fid INTEGER NOT NULL,
	status _STATUS,
	PRIMARY KEY (rnum),
	FOREIGN KEY (cid) REFERENCES Customer(id),
	FOREIGN KEY (fid) REFERENCES Flight(fnum)
);

CREATE TABLE FlightInfo
(
	fiid INTEGER NOT NULL,
	flight_id INTEGER NOT NULL,
	pilot_id INTEGER NOT NULL,
	plane_id INTEGER NOT NULL,
	PRIMARY KEY (fiid),
	FOREIGN KEY (flight_id) REFERENCES Flight(fnum),
	FOREIGN KEY (pilot_id) REFERENCES Pilot(id),
	FOREIGN KEY (plane_id) REFERENCES Plane(id)
);

CREATE TABLE Repairs
(
	rid INTEGER NOT NULL,
	repair_date DATE NOT NULL,
	repair_code _CODE,
	pilot_id INTEGER NOT NULL,
	plane_id INTEGER NOT NULL,
	technician_id INTEGER NOT NULL,
	PRIMARY KEY (rid),
	FOREIGN KEY (pilot_id) REFERENCES Pilot(id),
	FOREIGN KEY (plane_id) REFERENCES Plane(id),
	FOREIGN KEY (technician_id) REFERENCES Technician(id)
);

CREATE TABLE Schedule
(
	id INTEGER NOT NULL,
	flightNum INTEGER NOT NULL,
	departure_time DATE NOT NULL,
	arrival_time DATE NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (flightNum) REFERENCES Flight(fnum)
);

----------------------------
-- INSERT DATA STATEMENTS --
----------------------------

COPY Customer (
	id,
	fname,
	lname,
	gtype,
	dob,
	address,
	phone,
	zipcode
)
FROM 'customer.csv'
WITH DELIMITER ',';

COPY Pilot (
	id,
	fullname,
	nationality
)
FROM 'pilots.csv'
WITH DELIMITER ',';

COPY Plane (
	id,
	make,
	model,
	age,
	seats
)
FROM 'planes.csv'
WITH DELIMITER ',';

COPY Technician (
	id,
	full_name
)
FROM 'technician.csv'
WITH DELIMITER ',';

COPY Flight (
	fnum,
	cost,
	num_sold,
	num_stops,
	actual_departure_date,
	actual_arrival_date,
	arrival_airport,
	departure_airport
)
FROM 'flights.csv'
WITH DELIMITER ',';

COPY Reservation (
	rnum,
	cid,
	fid,
	status
)
FROM 'reservation.csv'
WITH DELIMITER ',';

COPY FlightInfo (
	fiid,
	flight_id,
	pilot_id,
	plane_id
)
FROM 'flightinfo.csv'
WITH DELIMITER ',';

COPY Repairs (
	rid,
	repair_date,
	repair_code,
	pilot_id,
	plane_id,
	technician_id
)
FROM 'repairs.csv'
WITH DELIMITER ',';

COPY Schedule (
	id,
	flightNum,
	departure_time,
	arrival_time
)
FROM 'schedule.csv'
WITH DELIMITER ',';

CREATE TRIGGER new_plane_entry BEFORE INSERT 
ON Plane FOR EACH ROW
EXECUTE PROCEDURE get_plane_id();

CREATE TRIGGER new_pilot_entry BEFORE INSERT 
ON Pilot FOR EACH ROW
EXECUTE PROCEDURE get_pilot_id();

CREATE TRIGGER new_tech_entry BEFORE INSERT 
ON Technician FOR EACH ROW
EXECUTE PROCEDURE get_tech_id();

CREATE TRIGGER new_flight_entry BEFORE INSERT 
ON Flight FOR EACH ROW
EXECUTE PROCEDURE get_flight_fnum();

CREATE TRIGGER new_flightinfo_entry BEFORE INSERT 
ON FlightInfo FOR EACH ROW
EXECUTE PROCEDURE get_flightinfo_fiid();

CREATE TRIGGER new_schedual_entry BEFORE INSERT 
ON Schedule FOR EACH ROW
EXECUTE PROCEDURE get_schedule_id();

