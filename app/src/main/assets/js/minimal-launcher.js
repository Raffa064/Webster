const appList = JSON.parse(app.getAppList());

const inputbar = createElement("input");
const matchedList = createElement("ul");

inputbar.oninput = () => {
  matchedList.innerHTML = "";

  appList
    .filter((app) => {
      const full = app.name + app.package;

      return full.includes(inputbar.value);
    })
    .forEach((app) => {
      matchedList.innerHTML += "<li>" + app.name + "<l1>";
    });
};

document.body.appendChild(inputbar);
document.body.appendChild(matchedList);

function createElement(tag) {
  return document.createElement(tag);
}
