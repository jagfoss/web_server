class Heating extends Component {
    constructor(name, initialTemp = 20) {
        super(name);
        this.temperature = initialTemp;
    }

    render(container) {
        const div = $(`
            <div class="compo" id="${this.name}_div">
                <span>${this.name}</span>
                <input type="number" class="temperature" value="${this.temperature}" min="10" max="25">
            </div>
        `);
        container.append(div);

        div.find(".temperature").change((e) => {
            const newTemp = e.target.value;
            this.updateState(parseInt(newTemp));

            $.ajax({
                url: `data/${this.name}.json`,
                method: "PUT",
                contentType: "application/json",
                data: JSON.stringify({temperature: newTemp}),
                success: () => console.log(`Sent ${newTemp} to server.`),
                error: () => console.log("Failed sending to server.")
            });
        });
    }

    fetchState(container) {
        $.getJSON(`data/${this.name}.json`, (data) => {
            this.updateState(data.temperature);
        });
    }

    updateState(newTemp) {
        this.temperature = newTemp;
        this.emitStateChanged();
    }

    emitStateChanged() {
        $(document).trigger("componentStateChanged", {
            name: this.name,
            type: "heating",
            temperature: this.temperature
        });
    }
}
