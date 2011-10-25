SET SERVEROUTPUT ON;

DECLARE
	x NUMBER;
	last_count PLS_INTEGER;
BEGIN
	IF sys_context('USERENV', 'CURRENT_SCHEMA') = 'SYSTEM' OR sys_context('USERENV', 'CURRENT_SCHEMA') = 'SYS' THEN
		DBMS_OUTPUT.PUT_LINE('Logged in as '||sys_context('USERENV', 'CURRENT_SCHEMA'));
		RETURN;
	END IF;
	last_count := -1;
	LOOP
		FOR r IN (select object_name, object_type from user_objects WHERE NOT object_name LIKE 'BIN%' AND NOT object_type = 'DATABASE LINK') LOOP
			BEGIN
				execute immediate 'DROP '||r.object_type||' '||r.object_name;
				DBMS_OUTPUT.PUT_LINE('DROP '||r.object_type||' '||r.object_name);
			EXCEPTION
				WHEN OTHERS THEN
					NULL;
			END;
		END LOOP;
		SELECT COUNT(*) INTO x FROM USER_OBJECTS WHERE NOT object_name LIKE 'BIN%';
		IF x = 0 THEN
			EXIT;
		END IF;
		IF last_count = -1 THEN
			last_count := x;
		ELSE
			IF (last_count = x) THEN
				FOR r IN (select object_name, object_type from user_objects WHERE NOT object_name LIKE 'BIN%') LOOP
					DBMS_OUTPUT.PUT_LINE('Remaining object '||r.object_type||' '||r.object_name);
				END LOOP;
				EXIT;
			ELSE
				last_count := x;
			END IF;
		END IF;
	END LOOP;
END;
/