const logEl = document.getElementById('log');
const log = text => logEl.innerHTML = `${logEl.innerHTML}\n${text}`;

for (var i = 0; i< 1; i++) {
(function(){
const j = i;
fetch('/api/hello')
  .then(response => (response.text()))
  .then(text => log(j + ":" + text))
  .catch(err => log(`Failed:\n${err.stack}`));
  })();
}