# Test Execution

This is a walk-through of some manual tests.  They help demonstrate the different actors 
and events that occur in an OpenADR-based system.

## Setup 

 You have to set up some default data before executing these tests.

###	Create Programs

- Access the *Program* tab
- Click the green *Create Program* button in the upper right
- Input the value `test-program-one` for Program Name and `test-uri-one` for URI
- Create a second program using `test-program-two` and `test-uri-two`
		
### Create Customers

- Switch to the *Customer*s tab
- Click the green *Create Customer* button in the upper right
- Select the Program from the drop down menu, in this case select `test-program-one`
- Input the value `test-name-one` for Customer Name, `test-customer-one` for VEN ID 
  and `test-client-uri-one` for the Client URI
- Repeat for `test-program-two`, `test-name-two`, `test-customer-two` and `test-client-uri-two`
		
### Create Events

- Switch to the *Events* tab
- Select the Program `test-program-one` from the drop down
- Input the value `test-event-one` for the Event ID, Priority can be any numeric value greater 
  than 1 (currently unused and do not include the ' in your input), and the start date/time 
  need to occur prior to the end date. You are allowed to create events with no duration as 
  well as events that have occurred and been completed in the past.
- Repeat for `test-program-two` and `test-event-two`
		
## HTTP Requests

- Click on the *Events* tab if you already aren't directed there after creating the two events
- Click on `test-event-one` to be directed to the VEN display page, should currently show 
  nothing as Push is not implemented in this revision
- Switch to the `/xmpp-http-tests` directory
- Using curl, input the command:
		
    curl -v -d @httpRequest1.xml -H "Content-type: application/xml" \
      http://localhost:9000/OpenADR2/Simple/EiEvent
			
- Wait for the response to refresh through AJAX or to refresh through the Refresh button in 
  the top right	
- Using curl, input the command
		
    curl -v -d @httpCreated1.xml -H "Content-type: application/xml" \
      http://localhost:9000/OpenADR2/Simple/EiEvent
		
- After pressing the *Refresh* button, *Status* should now read "Opted In" and be a green 
  button instead of the grey "Pending"
		
## XMPP Requests

- Click on the *Select Event* drop down menu and select `test-event-two`
- The user receiving the message in Openfire is to be named `xmpp-vtn` and the password 
  `xmpp-pass`
- **IMPORTANT** Modify the `xmppCreated2.xml` and `xmppRequest2.xml` to be the correct 
  JID for the `to=""` (ex. `to="xmpp-vtn@localhost/vtn"`)
- Connect to Psi and open the XML Console. From the `xmppRequest1.xml` file copy and paste 
  the content into XML Input...
- Wait for the response to refresh through AJAX or using the *Refresh* button in 
  the top right
- From the `xmppCreated1.xml` file copy and paste the content into the XML Input... in Psi
- After pressing the *Refresh* button, *Status* should now read "Opted Out" and be a red 
  button instead of the grey "Pending"
