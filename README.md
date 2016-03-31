# Popular-Movies-App
An app to allow users to discover popular and highly rated movies on the web.

Features:
Movies are displayed in the main layout via a grid of their corresponding movie poster thumbnails
UI contains an element (settings menu) to toggle the sort order of the movies by: most popular, highest rated, and favorites
UI contains a screen for displaying the details for a selected movie.
Movie Details layout contains title, release date, movie poster, vote average, and plot synopsis.
Movie Details layout contains a section for displaying trailer videos and user reviews
Tablet UI uses a Master-Detail layout implemented using fragments. 
The left fragment is for discovering movies. The right fragment displays the movie details view for the currently selected movie.
When a user changes the sort criteria (most popular, highest rated, and favorites) the main view gets updated correctly.
When a movie poster thumbnail is selected, the movie details screen is launched [Phone] or displayed in a fragment [Tablet]
When a trailer is selected, app launches the trailer.
In the movies detail screen, a user can tap a button(a star) to mark it as a Favorite
App persists favorite movie details using a database
