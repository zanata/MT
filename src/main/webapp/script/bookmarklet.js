function requestTranslations() {
  console.info('Requesting translations...');
  // var BASE_URL = '//mt-zanata.itos.redhat.com/';
  var BASE_URL = '//' + window.location.hostname + (window.location.port ? (':' + window.location.port) : '') + '/';
  var baseRestUrl = BASE_URL + 'api/translate';
  var DEFAULT_SRC_LANG = 'en';
  var LANG_PREFIX_CLASS = 'i18n-';

  /*
   check which language the user has selected.
   The only way right now to get the selected lang is through class in body which starts with i18n-
   */
  // var pageLang = jQuery('html').attr('lang');
  var classes = jQuery('body').attr('class').split(' ');
  var pageLang = undefined;
  for(var i in classes) {
    if (classes[i].startsWith(LANG_PREFIX_CLASS) ) {
      pageLang = classes[i].replace(LANG_PREFIX_CLASS, '');
      break;
    }
  }
  if (typeof pageLang === 'undefined' || pageLang === DEFAULT_SRC_LANG) {
    alert('Please choose a language from the top menu.');
    return false;
  }
  var contentLang = jQuery('html head meta[name=language]').attr('content');
  if (contentLang !== DEFAULT_SRC_LANG) {
    alert('Unable to translate from content language: ' + contentLang);
    return false;
  }
  toggleLoading(true);
  var restUrl = baseRestUrl + '?sourceLang=' + contentLang + '&targetLang='
      + pageLang;
  // extract the page title and article div for translation
  var pageTitle = jQuery('html head title');
  var divContent = jQuery('article#main-content div.content-wrapper');
  var sourceData = {
    title : pageTitle.text(),
    divContent : divContent.html(),
    url: window.location.href
  };

  jQuery.ajax({
    url : restUrl,
    method : 'POST',
    type: 'POST',
    data : JSON.stringify(sourceData),
    async: false,
    cache: false,
    headers : {
      'Accept' : 'application/json',
      'Content-Type' : 'application/json',
      'Access-Control-Allow-Origin': '*'
    },
    dataType: 'json',
    crossDomain: true,
    success: function (response) {
      pageTitle.text(response.title);
      divContent.html(response.divContent);
      console.info('Translation completed');
      toggleLoading(false);
    },
    error : function(jqXHR, textStatus, errorThrown) {
      alert('Translation error');
      console.error(textStatus, errorThrown);
      toggleLoading(false);
    }
  });
}

function toggleLoading(show) {
  var id = 'loading-div';
  var loadingDiv = jQuery('#' + id);
  if (show) {
    if (loadingDiv.length) {
      loadingDiv.css('display', 'block');
    } else {
      var frag = document.createDocumentFragment();
      var temp = document.createElement('div');
      temp.setAttribute('id', id);
      temp.innerHTML = "<div style='position:absolute; background:white; font-size:2em; display:block;padding:0.5em; z-index:10000;box-shadow:0 2px 4px 0px rgba(0,0,0,0.1)'>Loading translations...</div>";
      frag.appendChild(temp);
      document.body.insertBefore(frag, document.body.childNodes[0]);
    }
  } else {
    if (loadingDiv) {
      loadingDiv.css('display', 'none')
    }
  }
}
