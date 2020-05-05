import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import { withRouter } from 'react-router-dom';

import './Tx.css';

class Transaction extends Component {
    constructor(props) {
        super(props);
    }

    handleChange(event) {
    }

    async handleSubmit(event) {
    }

    render() {

        const { txid, sender, receiver, value, inputs, outputs, confirmed} = this.props;

        const outputsList = outputs.map((output,index) => (
            <li key={index}>
                {output.to} {output.value} TC
            </li>
        ));


        return (
            <div className="transaction">
                <div>
                    Hash: {txid}
                </div>

                {/* add inputs */}

                <div>
                    { (sender === "") ? "COINBASE" : sender} { value } TC
                    <ul>
                        { outputsList }
                    </ul>
                </div>

                <div>
                    { confirmed == true ?  <h3 id="confirmed-tx">CONFIRMED</h3> : <h3 id="unconfirmed-tx">UNCONFIRMED</h3>}
                </div>
            </div>
        );
    }
}

export default withRouter(Transaction);