var keycloak = new Keycloak();
var serviceUrl = 'http://127.0.0.1:3000/service'

function notAuthenticated() {
    document.getElementById('not-authenticated').style.display = 'block';
    document.getElementById('authenticated').style.display = 'none';
}

function authenticated() {
    document.getElementById('not-authenticated').style.display = 'none';
    document.getElementById('authenticated').style.display = 'block';
    document.getElementById('message').innerHTML = 'User: ' + keycloak.tokenParsed['preferred_username'];
}

function request(endpoint) {
    console.log(endpoint + '1');
    
    var output = document.getElementById('message');
    var req = new XMLHttpRequest();

    req.open('GET', serviceUrl + '/' + endpoint, true);

    if (keycloak.authenticated) {
        req.setRequestHeader('Authorization', 'Bearer ' + keycloak.token);
    }
    
    req.onreadystatechange = function () {
        if (req.readyState == 4) {
            if (req.status == 200) {
                output.innerHTML = 'Amount: ' + JSON.parse(req.responseText).amount;
            } else if (req.status == 0) {
                output.innerHTML = '<span class="error">Request failed</span>';
                // because im not sure ow to bypass cors at this point.
                if (!keycloak.authenticated) {
                    keycloak.login();
                }
            } else {
                output.innerHTML = '<span class="error">' + req.status + ' ' + req.statusText + '</span>';
                if (!keycloak.authenticated) {
                    keycloak.login();
                }
            }
        }
    };
    
    if (keycloak.authenticated) {
        let minValidity = 5;  // 5 seconds
        keycloak.updateToken(minValidity).success(function(refreshed) {
          if (refreshed) {
            console.log('Token was successfully refreshed');
          } else {
            console.log('Token is still valid');
          }
          req.send();
        }).error(function() {
          console.error('Failed to refresh the token, or the session has expired');
        });
    } else {
        req.send();
    }
}

window.onload = function () {
    keycloak.init({ onLoad: 'check-sso', checkLoginIframeInterval: 1 }).then(function () {
        if (keycloak.authenticated) {
            authenticated();
        } else {
            console.log("NOT AUTHENTICATED")
            notAuthenticated();
        }

        document.body.style.display = 'block';
    });
}

keycloak.onAuthLogout = notAuthenticated;
