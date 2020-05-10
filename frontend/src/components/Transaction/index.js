import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import { Link } from 'react-router-dom';

import { withRouter } from 'react-router-dom';

import './Tx.css';

class Transaction extends Component {
    constructor(props) {
        super(props);
    }

    render() {

        const { txid, sender, receiver, value, inputs, outputs, verified , isSent} = this.props;

        const outputsList = outputs.map((output,index) => (
            <li key={index}>
                <span className="tx-span">Receiver:</span> <Link to={"/address/"+ encodeURIComponent(output.to) }> {output.to.substring(0,3) + "..." + output.to.substring(output.to.length - 30, output.to.length)} </Link> {output.value} TC
            </li>
        ));

        return (
            <div className="transaction">
                <div>
                <span className="tx-span">Txid:</span> <Link to={"/tx/"+ txid} > {txid} </Link>
                </div>
                    
                <div>
                <span className="tx-span">Value:</span> {(isSent != true) ? <span id="minus"> - {value} </span> : <span id="plus"> {value} </span>} 
                </div>

                {/* add inputs */}

                <div>
                <span className="tx-span">Sender:</span> { (sender === "") ? "COINBASE" : <Link to={"/address/"+ encodeURIComponent(sender) }> {sender.substring(0, 3) + "..." + sender.substring(sender.length - 30, sender.length)} </Link>} 
                    <ul>
                        { outputsList }
                    </ul>
                </div>

                <div>
                    { verified == true ?  <h3 id="confirmed-tx">CONFIRMED</h3> : <h3 id="unconfirmed-tx">UNCONFIRMED</h3>}
                </div>
            </div>
        );
    }
}

export default withRouter(Transaction);