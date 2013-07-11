Setup:

	1.	Create Programs
		a. Access the Program tab
		b. Click the green Create Program button in the upper right
		c. Input the value 'test-program-one' for Program Name and 'test-uri-one' for URI, 
			(do not include the ')
		d. Repeat for 'test-program-two' and 'test-uri-two'
		
	2.	Create Customers
		a. Switch to the Customers tab
		b. Click the green Create Customer button in the upper right
		c. Select the Program from the drop down menu, in this case select 'test-program-one'
		d. Input the value 'test-name-one' for Customer Name, 'test-customer-one' for VEN ID 
			and 'test-client-uri-one' for the Client URI 
			(Client URI in this revision does nothing, do not include the ' for your input)
		e. Repeat for 'test-program-two', 'test-name-two', 'test-customer-two' and 'test-client-uri'two'
		
	3. 	Create Events
		a. Switch to the Events tab
		b. Select the Program 'test-program-one' from the drop down
		c. Input the value 'test-event-one' for the Event ID, Priority can be any numeric value greater 
			than 1 (currently unused and do not include the ' in your input), and the start date/time 
			need to occur prior to the end date. You are allowed to create events with no duration as 
			well as events that have occurred and been completed in the past.
		d. Repeat for 'test-program-two' and 'test-event-two'
		
	4. HTTP Requests
		a. Click on the Events tab if you already aren't directed there after creating the two events
		b. Click on test-event-one to be directed to the VEN display page, should currently show nothing
			as Push is not implemented in this revision
		c. Switch to the /xmpp-http-tests directory
		d. Using curl, input the command
		
			curl -v -d @httpRequest1.xml -H "Accept:application/xml" -H "Content-Type:application/xml" http://localhost:8080/oadr2-vtn-groovy/OpenADR2/Simple/EiEvent
    			
		e. Wait for the response to refresh through AJAX or to refresh through the Refresh button in the top right
		
		f. Using curl, input the command
		
			curl -v -d @httpCreated1.xml -H "Accept:application/xml" -H "Content-Type:application/xml" http://localhost:8080/oadr2-vtn-groovy/OpenADR2/Simple/EiEvent
		
		g. Using refresh, Status should now read Opted In and be a green button instead of the grey Pending
		
	5. XMPP Requests
		a. Click on the  Select Event: drop down menu and select 'test-event-two'
		b. The user receiving the message in Openfire is to be named 'xmpp-vtn' and the password 'xmpp-pass'
		c. ***IMPORTANT*** Modify the xmppCreated2.xml and xmppRequest2.xml to be the correct Session ID for the
			to="" (ex. to="xmpp-vtn@msawant-mbp.local/vtn")
		d. Connect to Psi and open the XML Console. From the xmppRequest2.xml file copy and paste the content into
			XML Input...
		e. Wait for the response to refresh through AJAX or to refresh through the Refresh button in the top right
		f. From the xmppCreated1.xml file copy and paste the content into the XML Input... in Psi
		g. Using refresh, Status should now read Opted Out and be a red button instead of the grey Pending
		