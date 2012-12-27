function CAWBrowser(){
	var haveFlash = new Array();
	var i;
	for (i=6; i<15; i++ ) haveFlash[i] = false;
	var ua = navigator.userAgent;
	this.msie = (ua && ( parseFloat( navigator.appVersion )  >=4 ) && ( ua.indexOf("Opera") < 0 ) && ( ua.indexOf("MSIE 4") < 0 ) && ( ua.indexOf( "MSIE" ) >=0) );
	this.win = (ua && ((ua.indexOf( "Windows 95" ) >=0) || (ua.indexOf("Windows NT") >=0 ) || (ua.indexOf("Windows 98") >=0) ) );
	this.mac = (navigator.platform && (navigator.platform.indexOf('Mac')!=-1));
	this.opera7 = ((ua.indexOf('Opera') != -1) && window.opera && document.readyState) ? 1 : 0;
	this.gecko   = (ua.toLowerCase().indexOf('gecko') != -1) && (ua.indexOf('safari') == -1);

	var flash_nonie = (navigator.mimeTypes && navigator.mimeTypes["application/x-shockwave-flash"]) ? navigator.mimeTypes["application/x-shockwave-flash"].enabledPlugin : 0;

	if( flash_nonie){
		for (i=6; i<15; i++ ){ 
			haveFlash[i] = flash_nonie;
			haveFlash[i] = (parseInt(haveFlash[i].description.substring(haveFlash[i].description.indexOf(".")-2))>=i);
		}
	}else if ( this.msie && this.win && !this.mac){
		for (i=6; i<15; i++ ) 
			try{ haveFlash[i]  = new ActiveXObject("ShockwaveFlash.ShockwaveFlash." + i); }catch(e){};
	}
	
	this.other = !( (this.gecko || this.msie) && this.win && !this.mac);

	this.flash = 0;
	for (i=6; i<15; i++ ) if (haveFlash[i]) this.flash = i;
}

function CACode(){
	this.write = function (aw_host, aw_swf_base, aw_gif_base, alt, aw_w, aw_h, sdp_ver, flash_clicks, show_stat, media_params, gif_tizer, proto){ 

		var seed = Math.round(Math.random()*65535);
		var url_params = "";
		if ( typeof( aw_url_params ) != "undefined") {
			url_params = "*url_params2=" + aw_url_params;
		}
		if (typeof(proto) == "undefined"){
			proto = "";
		}

		if(proto && proto.length > 2 && proto.indexOf(":") < 0 ){ 
			proto = proto + ":";
		}

		if (show_stat){
			var im = new Image();
			var pix_url = new String(show_stat);
			pix_url = pix_url.replace("%aw_random%", seed);
			im.src = pix_url;
		}

		var subsection = 0;
		
		if (typeof( awaps_get_home_type ) != "undefined"){
			var s = awaps_get_home_type();
			if (s >=0 ) subsection = s;
		}else if (typeof( awaps_is_new_home ) != "undefined"){
			subsection = awaps_is_new_home() ? 101 : 0;
		}

		var aw_br = new CAWBrowser();
		var aw_code;

		if ( (aw_br.flash >= 8) && aw_swf_base ){ 
			var aw_show_url =  proto + '//' + aw_host + '/0/' + aw_swf_base + '&awcode=1&sdp_ver=' + sdp_ver + '&flash=' + aw_br.flash;
			var aw_click_url = proto + '//' + aw_host + '/1/' + aw_swf_base ;	

			if (subsection){
				aw_show_url  += '&subsection=' + subsection;
				aw_click_url += '%26subsection=' + subsection;
			}

			var c_linkNmbs = 'link1=' + aw_click_url;


			for (var n_linkNmb = 1; n_linkNmb <= flash_clicks; n_linkNmb++) {
				c_linkNmbs += '&link' + (n_linkNmb + 1) + '=' + aw_click_url + '%26click_num=' + n_linkNmb;

			}

			if (media_params) c_linkNmbs += '&' + media_params;

			var awswfsrc = aw_show_url;

			aw_code = '<object classid=clsid:D27CDB6E-AE6D-11cf-96B8-444553540000 codebase=http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0 width=' + aw_w + ' height=' + aw_h + ' >'   
				+ '<param name="allowScriptAccess" value="Always" ><param name=movie value="'+ awswfsrc +'" ><param name=menu value=false><param name=quality value=high><param name=wmode value=opaque>' 
				+ '<param name="flashvars" value="'+ c_linkNmbs + '" >'
				+ '<EM' + 'BED src="' + awswfsrc + '"  flashvars="' + c_linkNmbs + '" quality=high '
				+ ' allowScriptAccess=Always wmode=opaque menu=false swLiveConnect=FALSE WIDTH='+ aw_w +' HEIGHT=' + aw_h 
				+ ' TYPE="application/x-shockwave-flash" PLUGINSPAGE="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash">'
				+ '</EMBED>'
				+ ' </object>'; 	  


		}else{ 

			var aw_show_url = proto + '//' + aw_host +'/0/' + aw_gif_base + '?flash=' + aw_br.flash;;
			var aw_click_url = proto + '//' + aw_host +'/1/' + aw_gif_base  + '?' + url_params;
			if (subsection){
				aw_show_url  += '&subsection=' + subsection;
				aw_click_url += '&subsection=' + subsection;
			}

			aw_code = '<img src="' + aw_show_url + '" width="' + aw_w + '" height="' + aw_h + '" border=0 alt=\"' + alt + '\" >';
			if (!gif_tizer)	aw_code = '<a href="' + aw_click_url  + '" target=_blank >' + aw_code + '</a>'; 
		}
		document.write( aw_code );
	}
}
