$(document).ready(() => {
    const panel = $("#control-panel");

    const components = [
        new Lighting("lighting"),
        new Heating("heating")
    ];

    // Trigger data fetching and rendering
    components.forEach(component => {
        component.fetchState(panel);
        component.render(panel);
    });

    // How to add a new component:
    // const curtain = new Curtain("curtain");
    // components.push(curtain);
    // curtain.fetchState(panel);
    // curtain.render(panel);
});

$(document).on("componentStateChanged", (e, data) => {
    if (data.type === "heating") {
        const $div = $(`#${data.name}_div`);
        $div.find(".temperature").val(data.temperature);
    } else if (data.type === "lighting") {
        const $div = $(`#${data.name}_div`);
        const isOn = data.lighting_state;
        $div.find(".toggle").text(isOn ? "ON" : "OFF");
        $div.removeClass("on off").addClass(isOn ? "on" : "off");
    }
});
