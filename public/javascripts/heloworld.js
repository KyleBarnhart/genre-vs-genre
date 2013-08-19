var duration = 1; // track the duration of the currently playing track
    $(document).ready(function() {
      $('#api').bind('ready.rdio', function() {
    	var that = $(this);
        $(this).rdio().play($('.playSong').first().data("key") );
      });
      $('#api').bind('playingTrackChanged.rdio', function(e, playingTrack, sourcePosition) {
        if (playingTrack) {
          duration = playingTrack.duration;
        }
        });
      $('#api').bind('positionChanged.rdio', function(e, position) {
        $('#position').css('width', Math.floor(100*position/duration)+'%');
      });
      
      // this is a valid playback token for localhost.
      // but you should go get your own for your own domain.
      //barnhart.ca
      $('#api').rdio('GAtSCX5ZAJuL8WR2cHlzNHd5ZXg3Z2M0OXdoaDY3aHdrbmJhcm5oYXJ0LmNhuYR3_rb-2S1gnUjTaE6t5g==');
      //localhost
      //$('#api').rdio('GAlNi78J_____zlyYWs5ZG02N2pkaHlhcWsyOWJtYjkyN2xvY2FsaG9zdEbwl7EHvbylWSWFWYMZwfc=');

      $('#play').click(function() { $('#api').rdio().play(); });
      
      $('.playSong').click(function() {
    	  var that = $(this);

    	  if(that.text() == "Stop") {
    		  $('#api').rdio().stop();
    		  $('.playSong').text("Play");
    	  } else {
    		  $('.playSong').text("Play");
    		  that.text("Stop");
    		  $('#api').rdio().play( $(this).data("key") );
    	  }
      });
      
      $('.pick').click(function(){
    	  var that = $(this);
    	  $('.pick').attr('disabled','disabled');
    	  document.location.href="/pick?genre=" + $(this).data("genre");
      });
      
      $('#positionBox').click(function(e){
    	  var offset = $(this).offset();
    	  var position = Math.abs(e.clientX - offset.left - (this.offsetWidth / 2))
						* $('.playSong:contains("Stop")').data("duration")
						/ (this.offsetWidth / 2);
    	  $('#api').rdio().seek( position );
      });
    });