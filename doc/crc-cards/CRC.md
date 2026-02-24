# Event (model)

| Responsibility                                            | Collaborators |
| :-------------------------------------------------------- | :------------ |
| Store event id (UUID)                                     | Image         |
| Store ids of interested entrants                          | User          |
| Store event details and entrant requirements/restrictions |               |
| Store image id                                            |               |

# User (model)

| Responsibility                                           | Collaborators |
| :------------------------------------------------------- | :------------ |
| Store profile details                                    | Entrant       |
| Store user id (UUID)                                     | Organizer     |
| Store device id (UUID)                                   | Admin         |
| Create Self                                              |               |
| Delete Self                                              |               |
| Compose permission structure (Entrant, Organizer, Admin) |               |
| Synchronize user model with database                     |               |

# Entrant (model)

| Responsibility                    | Collaborators |
| :-------------------------------- | :------------ |
| Store entered events ids          | User          |
| Store event notification settings | Event         |
| Enter events                      |               |
| Leave events                      |               |

# Organizer (model)

| Responsibility                   | Collaborators |
| :------------------------------- | :------------ |
| Create events -Manage own events | User          |
| Store created events ids         | Event         |
| Store sent notification ids      |               |

# Admin (model)

| Responsibility              | Collaborators |
| :-------------------------- | :------------ |
| View & delete other users   | User          |
| View & delete events        | Entrant<br>   |
| View & delete images        | Organizer     |
| View & delete notifications | Admin         |
|                             | Event         |
|                             | Image         |

# Notification (model)

| Responsibility               | Collaborators |
| :--------------------------- | :------------ |
| Store notification id (UUID) | -User         |
| Store notification message   |               |
| Store sender id              |               |
| Store all recipient ids      |               |

# Image (model)

| Responsibility            | Collaborators |
| :------------------------ | :------------ |
| Store image id (UUID)     | Event         |
| Store image data (base64) |               |

# ExternalUser (model)

| Responsibility                | Collaborators |
| :---------------------------- | :------------ |
| Store an external user’s id   | User          |
| Store an external user’s name | Event         |
|                               | Admin         |

---

# UserViewModel (view-model)

| Responsibility                                                      | Collaborators |
| :------------------------------------------------------------------ | :------------ |
| Getting and setting profile details                                 | User          |
| Getting nullable composables permissions structure (null or exists) | Entrant       |
| Performing fallible operations on composable permission structure   | Organizer     |
| Hydrate and update model from remote data                           | Admin         |

# EventViewModel (view-model)

| Responsibility                                  | Collaborators |
| :---------------------------------------------- | :------------ |
| Getting and setting event details               | Event         |
| Provide event models with data                  |               |
| Getting and setting event models’ entrant lists |               |

# MultiEventViewModel (view-model)

| Responsibility                                    | Collaborators |
| :------------------------------------------------ | :------------ |
| Getting list of data about events                 | Event         |
| Update the view with events to be displayed       |               |
| Update the view listings based on applied filters |               |
| Hydrate and update model from remote data         |               |

# NotificationViewModel (view-model)

| Responsibility                                                                          | Collaborators |
| :-------------------------------------------------------------------------------------- | :------------ |
| Getting and setting new notifications to be displayed based on notification preferences | Notification  |
|                                                                                         | User          |
|                                                                                         | Entrant       |

# MultiNotificationViewModel (view-model)

| Responsibility                       | Collaborators |
| :----------------------------------- | :------------ |
| Getting list of notification details | Notification  |
| Provide data to notification models  |               |
| Update notification list views       |               |

# MultiImageViewModel (view-model)

| Responsibility                                       | Collaborators |
| :--------------------------------------------------- | :------------ |
| Supply existing images to views                      | Image         |
| Create new images in the model                       |               |
| Hydrate and update the image model from the database |               |

# MultiExternalUserViewModel (view-model)

| Responsibility                                | Collaborators |
| :-------------------------------------------- | :------------ |
| Hydrate and update the list of external users | ExternalUser  |

---

# UserSettingsFragment (view)

| Responsibility                    | Collaborators |
| :-------------------------------- | :------------ |
| Displays editable profile details | UserViewModel |

# EventBrowserFragment (view)

| Responsibility                              | Collaborators       |
| :------------------------------------------ | :------------------ |
| Displays a list of available events to join | MultiEventViewModel |
|                                             | EventListAdapter    |

# EventCreationFragment (view)

| Responsibility                                                                   | Collaborators  |
| :------------------------------------------------------------------------------- | :------------- |
| Displays editable event details                                                  | EventViewModel |
| Displays an option to edit event images Displays an option to save event details |                |

# EventDetailsFragment (view)

| Responsibility                                                                                 | Collaborators  |
| :--------------------------------------------------------------------------------------------- | :------------- |
| Displays the details of a specific event Displays how many people are interested in this event | EventViewModel |
| Display options to join/exit an event wait list for entrants                                   | UserViewModel  |
| Display enrollment and management options as appropriate                                       | ImageViewModel |

# EventListFragment (view)

| Responsibility                                                                     | Collaborators      |
| :--------------------------------------------------------------------------------- | :----------------- |
| Displays events for which the user has joined the wait list/indicated interest for | EventListViewModel |

# ScanQrFragment (view)

| Responsibility              | Collaborators  |
| :-------------------------- | :------------- |
| Displays device camera view | EventViewModel |

# FilterFragment (view)

| Responsibility                    | Collaborators       |
| :-------------------------------- | :------------------ |
| Display editable filtering fields | MultiEventViewModel |

# HistoryFragment (view)

| Responsibility                                                | Collaborators       |
| :------------------------------------------------------------ | :------------------ |
| Displays a list of events the user has enrolled in previously | UserViewModel       |
|                                                               | MultiEventViewModel |
|                                                               | EventListAdapter    |

# HomeFragment (view)

| Responsibility                                  | Collaborators              |
| :---------------------------------------------- | :------------------------- |
| Displays notifications sent to a user           | MultiNotificationViewModel |
| Displays other relevant information to the user | NotificationListAdapter    |

# EventManagerFragment (view)

| Responsibility                                             | Collaborators |
| :--------------------------------------------------------- | :------------ |
| Displays various options for organizers surrounding events |               |

# WaitListFragment (view)

| Responsibility                                                       | Collaborators   |
| :------------------------------------------------------------------- | :-------------- |
| Displays a list of entrants who are in the waiting list for an event | EventViewModel  |
| Allow for CSV export of the user list                                | UserListAdapter |

# NotificationEditFragment (view)

| Responsibility                                                                                | Collaborators         |
| :-------------------------------------------------------------------------------------------- | :-------------------- |
| Displays editable fields about notification details                                           | NotificationViewModel |
| Display an option to send notification Display options to send to specific groups of entrants |                       |

# EventDashboardFragment (view)

| Responsibility                                                                            | Collaborators       |
| :---------------------------------------------------------------------------------------- | :------------------ |
| Displays a list of all events created by an organizer Display an option to add new events | MultiEventViewModel |
|                                                                                           | EventListAdapter    |

# NotificationHistoryFragment (view)

| Responsibility                                               | Collaborators              |
| :----------------------------------------------------------- | :------------------------- |
| Displays a list of notifications previously sent by the user | MultiNotificationViewModel |
|                                                              | NotificationListAdapter    |

# AdminImageBrowserFragment (view)

| Responsibility                                | Collaborators       |
| :-------------------------------------------- | :------------------ |
| Displays a list of images form across the app | MultiImageViewModel |
| Displays options to remove images             | ImageListAdapter    |

# AdminProfileBrowserFragment (view)

| Responsibility                           | Collaborators              |
| :--------------------------------------- | :------------------------- |
| Displays a list of user profiles         | MultiExternalUserViewModel |
| Displays options to remove user profiles | ExternalUserListAdapter    |

# AdminPanelFragment (view)

| Responsibility                     | Collaborators |
| :--------------------------------- | :------------ |
| Displays options for admin actions |               |

---

# ExternalUserListAdapter (array adapter)

| Responsibility                                                       | Collaborators    |
| :------------------------------------------------------------------- | :--------------- |
| Convert external user objects into viewable elements to be displayed | WaitListFragment |

# EventListAdapter (array adapter)

| Responsibility                                               | Collaborators        |
| :----------------------------------------------------------- | :------------------- |
| Convert event objects into viewable elements to be displayed | EventBrowserFragment |
|                                                              | EventListFragment    |
|                                                              | DashboardFragment    |
|                                                              | HistoryFragment      |

# NotificationListAdapter (array adapter)

| Responsibility                                                      | Collaborators               |     |
| :------------------------------------------------------------------ | :-------------------------- | --- |
| Convert notification objects into viewable elements to be displayed | HomeFragment                |     |
|                                                                     | NotificationHistoryFragment |     |

# ImageListAdapter (array adapter)

| Responsibility                                                                | Collaborators             |
| :---------------------------------------------------------------------------- | :------------------------ |
| Convert image objects into viewable elements to be displayed in a list format | AdminImageBrowserFragment |
