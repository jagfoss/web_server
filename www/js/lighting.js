class Lighting extends Component {
    constructor(name, initialState = false) {
        super(name);
        this.lighting_state = initialState;
    }

    render(container) {
        const div = $(`
            <div class="compo off" id="${this.name}_div">
                <span>${this.name}</span>
                <button class="toggle">${this.lighting_state ? "ON" : "OFF"}</button>
            </div>
        `);
        container.append(div);

        div.find(".toggle").click(() => {
            const newState=!this.lighting_state;
            this.updateState(newState);

            $.ajax({
                url: `data/${this.name}.json`,
                method: "PUT",
                contentType: "application/json",
                data: JSON.stringify({lighting_state: newState}),
                success: () => console.log(`Sent ${newState} to server`),
                error: () => console.log("Failed sending to server.")
            });
        });
    }

    fetchState(container) {
        $.getJSON(`data/${this.name}.json`, (data) => {
            this.updateState(data.lighting_state);
        });
    }

    updateState(newState) {
        this.lighting_state = newState;
        this.emitStateChanged();
    }

    emitStateChanged() {
        $(document).trigger("componentStateChanged", {
            name: this.name,
            type: "lighting",
            lighting_state: this.lighting_state
        });
    }
}
