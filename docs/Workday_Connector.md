#Workday<sup>&reg;</sup> to Active Directory (AD) Integration Powered by RoboMQ

[Workday<sup>&reg;</sup>](https://www.workday.com/) is a leading enterprise Software-as-a-Service (SaaS) application provider for human resource, finance, and Enterprise Resource Planning (ERP) teams. Compared to traditional on-premise Human Capital Management (HCM) and finance applications, Workday<sup>&reg;</sup> is [often recognized for its quick implementation and ease of use](https://searchhrsoftware.techtarget.com/definition/Workday), which makes the platform attractive to citizen integrators and line of business users.  

With Workday’s<sup>&reg;</sup> Human Capital Management (HCM), HR teams can perform an array of functions to manage the full lifecycle of an employee – from recruiting, talent management, workforce planning, expense management, and more.  

[Microsoft Active Directory](https://docs.microsoft.com/en-us/previous-versions/windows/it-pro/windows-server-2003/cc782657(v=ws.10)) and [Azure Active Directory](https://azure.microsoft.com/en-us/services/active-directory/), also referred to as AD, are on-premise and cloud hosted solutions, respectively, to manage users, roles, and computers and devices on a corporate network. AD allows network admins to create and manage domains, users, security groups, organization units and more. Companies leverage AD to organize a large number of users into logical groups and subgroups to provide Role-Based Access Control (RBAC) across the organization implementing job-specific or need-to-know basis privilege.  

##Benefits


| ![](https://robomq.io/wp-content/uploads/2020/02/workday-experience.png) | ![](https://robomq.io/wp-content/uploads/2019/10/benefit-2-1.png) | ![](https://robomq.io/wp-content/uploads/2020/02/workday-communication.png) |
|---|---|---|
| Improved “First day at Job” experience | Prevent Security and Reputation risk with role-based access control | Real time sync between HRIS, Active Directory or Azure AD |


##RoboMQ's Workday<sup>&reg;</sup> Connector

For many organizations, the process to get employees onboarded efficiently is a challenge. There tends to be a lot of moving parts behind the scenes to ensure employees have access to the tools and technologies needed to hit the ground running. System administrators are also often overloaded with related service desk tickets. Creating and/or updating user identities and assigning role-based access to employees can be tedious and costly work for them.  

The Workday<sup>&reg;</sup> to AD Integration powered by RoboMQ is one of our popular integration solutions that streamlines creation and updating of user identities in AD including and provisioning and assigning role based access to third party systems like Salesforce, ServiceNow, ERP, databases and others..  

> What once would take an average of two (2) hours or more of a system admin’s time to onboard, update critical employee lifecycle events, send out welcome or termination emails, create or delete accounts in various enterprise systems could now be fully automated with RoboMQ integration solution.  

As a full featured leading integration platform, RoboMQ provides two integration paths to achieve this functional need. 

1. Workday<sup>&reg;</sup> extract file-based Integration 
1. Integration using Workday<sup>&reg;</sup> APIs 

You could choose either of the two approaches based on your Workday setup or preferences. 

##Workday<sup>&reg;</sup> extract file-based integration to AD

The Workday<sup>&reg;</sup> file extract-based integration approach is a pretty straightforward process. Using this integration approach, scheduled extracts are setup to run on Workday<sup>&reg;</sup> on a periodic basis. These scheduled extracts can be setup to automatically run for new employees being hired, terminated employees, and other employee lifecycle event that need to update Active Directory (AD) such as employee role, location or reporting structure changes. The output format of these scheduled extract is typically CSV (Comma Separated Values). These extracts are sent automatically to RoboMQ for processing.  

Once the scheduled extracts are received on RoboMQ cloud, it is processed in near-real time to make updates to on-premise AD, Azure AD or cloud hosted AD like Microsoft AD on AWS.  

Below is a detailed view of how this integration solution can manage the employee lifecycle including role-based access: 

![](https://robomq.io/wp-content/uploads/2020/03/Workday-Integration-flow.png)
*Fig 1: Detailed view of managing employee lifecycle and role-based access*

Here’s a breakdown of how this process typically works during the employee onboarding: 

1. When a new employee starts working at any company location, their information is entered into the company’s Workday<sup>&reg;</sup> HRIS system. Workday<sup>&reg;</sup> sends this information as an extract to RoboMQ 

1. For new employees, RoboMQ creates user identity in Active Directory or Azure AD and provisions their access into enterprise applications based on their role or security groups ensuring that each employee has a single and unique account across all systems with associated access levels.  

1. RoboMQ automatically generates a random password for new employee along with default attributes and account control settings.  

1. A welcome email or other on-boarding emails are sent to the responsible office manager/s or helpdesk based on job role and location. These emails can be customized and branded to the organization needs. 
  
1. For existing employees, RoboMQ updates account settings, attributes, change of roles (e.g. promotions, transfer, etc.), or change of status (i.e. long-term leave, termination, or re-hire) in sync with Workday<sup>&reg;</sup> HRIS. 

In addition to the Workday<sup>&reg;</sup> to AD or Azure AD integration, additional downstream application connectors can be added to this workflow to update user account or access in third party systems. For example, some customers choose to accounts created in Salesforce, Office 365 or other enterprise systems. Managing user accounts or licenses in downstream systems has additional cost savings by making sure terminated employee do not have licenses assigned to them. More than the license cost, there is the safety mechanism in place so that terminated employee do not walk away with access to privileged CRM, operations or ERP systems. 

For more information or to set up a discussion on this integration solution, [schedule an appointment with a RoboMQ integration solution specialist today!](https://app.hubspot.com/meetings/eric-koch) 

## Workday<sup>&reg;</sup> to AD API and Data Integration Approach Using Connect iPaaS 

[Connect iPaaS](https://www.robomq.io/connect/) is the no-code citizen integrator approach to API and data integration between leading SaaS, EPR, CRM and operations applications on cloud or on-premise. Using simple, easy and intuitive drag-and-drop capabilities, users of Connect can create integration workflows that streamline and automate business processes in minutes with absolutely no coding! 

Workday<sup>&reg;</sup>, Active Directory (AD) and Azure AD are one of the many application connectors available on the Connect iPaaS. End user can integrate APIs from Workday<sup>&reg;</sup> to onboard new employees by creating user identities in AD or Azure AD using data mapping and workflow definition. Additional functionality can be added to the integration workflows such as creating a service desk ticket in ServiceNow, creating an account in Salesforce or Microsoft Dynamics CRM or adding an entry in QuickBooks. The main difference between this approach is that API calls are being made to perform data integrations based on specific events, versus files being sent to make updates in near real-time using a managed file transfer (MFT) mechanism.  

Let’s go over the process of setting up this integration on Connect iPaaS. In this example, we will integrate Workday<sup>&reg;</sup> to AD with ServiceNow as an additional connector.  

We start with the “Design” dashboard on Connect iPaaS where you have access to hundreds of APIs from leading SaaS, CRM, ERP and enterprise applications and databases.

![](https://robomq.io/wp-content/uploads/2020/03/RTD-Screen1.png)
*Fig 2: Connect iPaaS Design Dashboard*

Below images shows the workflow setup for employee onboarding from Workday<sup>&reg;</sup> to AD. The left side panel shows the sequence API integrations with first node, Workday<sup>&reg;</sup>, being the trigger event. This node receives API call when a new employee is hired in Workday<sup>&reg;</sup>. Workday<sup>&reg;</sup> is the trigger node while Active Directory and ServiceNow are the action nodes connected to the trigger node.

![](https://robomq.io/wp-content/uploads/2020/03/RTD-Screen2.png)
*Fig 3: Design dashboard with the workflow set up to streamline employee onboarding*

Now we go in the details of the Active Directory action node that is triggered in response to new employee hiring in Workday<sup>&reg;</sup>. Below image shows that a new user will be created in a specified Organization Unit for the new employee. 

![](https://robomq.io/wp-content/uploads/2020/03/RTD-Screen3.png)
*Fig 4: Create New User in Active Directory action being taken within Active Directory*

As we mentioned above, an additional ServiceNow API node is added to this workflow which will create a ServiceNow incident to notify a new hire to helpdesk. This incident can be used as a trigger to complete employee on-boarding checklist in service desk which may involve allocating laptop, mobile or other workplace setup activity. 

![](https://robomq.io/wp-content/uploads/2020/03/RTD-Screen4.png)
*Fig 5: An incident is created in ServiceNow to notify helpdesk of a new hire*

As you see in the image above, you can perform data mapping from Workday<sup>&reg;</sup> fields of the employee to required data fields is the action nodes, in this case being ServiceNow. Connect iPaaS provides advanced data mapping and transformation capabilities for business or non-technical users. You can use Microsoft Excel style function that you are very much at ease to perform desired data mappings and transformations. 

To start using Connect iPaaS, simply sign up at [https://trial.robomq.io/connect/](https://trial.robomq.io/connect/). Or for more information using this integration  approach, [schedule an appointment with a RoboMQ integration solution specialist today!](https://app.hubspot.com/meetings/eric-koch) 

>This integration provides a fabulous “first day at job” experience to new hires which can improve employee satisfaction, loyalty and retention.  The automated management of role-based access ensures that the right people have access to right information at right time. Managing the termination of access makes sure there are no compliance, security or reputation risks by someone walking away with access to sensitive privileged information. 

 

In summary, the Workday<sup>&reg;</sup> to AD integration powered by RoboMQ enables your IT and HR teams to focus on more strategic business priorities. This integration provides significant cost savings on HR and already burdened system admin resources.  Once this solution is implemented, new employees will have access to the accounts and applications needed from day one, and updates to their profile in your Active Directory will happen in near real-time. This integration provides a fabulous “first day at job” experience to new hires which can improve employee satisfaction, loyalty and retention.  The automated management of role-based access ensures that the right people have access to right information at right time. Managing the termination of access makes sure there are no compliance, security or reputation risks by someone walking away with access to sensitive privileged information.  

Experience the transformational power of our Workday<sup>&reg;</sup> to AD integration powered by RoboMQ for your enterprise. [Schedule a call with an integration specialist today](https://app.hubspot.com/meetings/eric-koch). 
